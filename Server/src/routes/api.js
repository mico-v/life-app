const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { authenticate, optionalAuth } = require('../middleware/auth');
const { tasks, clients, profiles } = require('../db/database');

/**
 * Health check endpoint
 */
router.get('/health', (req, res) => {
    res.json({
        success: true,
        message: 'Life App Server is running',
        serverTime: Date.now()
    });
});

/**
 * Sync tasks
 * POST /api/v1/sync
 */
router.post('/sync', authenticate, async (req, res, next) => {
    try {
        const { tasks: clientTasks, last_sync: lastSync, profile } = req.body;
        const clientToken = req.clientToken;
        
        // Register/update client
        const existingClient = await clients.findOne({ clientToken });
        if (!existingClient) {
            await clients.insert({
                clientToken,
                createdAt: Date.now(),
                lastSyncAt: Date.now()
            });
        } else {
            await clients.update(
                { clientToken },
                { $set: { lastSyncAt: Date.now() } }
            );
        }
        
        // Update profile if provided
        if (profile) {
            await profiles.update(
                { clientToken },
                {
                    clientToken,
                    displayName: profile.displayName || 'Life App User',
                    motto: profile.motto || 'Push to Start, Pop to Finish',
                    status: profile.status || 'Available',
                    updatedAt: Date.now()
                },
                { upsert: true }
            );
        }
        
        // Process tasks from client
        const serverTasks = [];
        
        if (clientTasks && Array.isArray(clientTasks)) {
            for (const task of clientTasks) {
                const existingTask = await tasks.findOne({
                    clientToken,
                    taskId: task.id
                });
                
                if (existingTask) {
                    // Update existing task
                    await tasks.update(
                        { _id: existingTask._id },
                        {
                            $set: {
                                ...task,
                                taskId: task.id,
                                clientToken,
                                updatedAt: Date.now()
                            }
                        }
                    );
                } else {
                    // Insert new task
                    await tasks.insert({
                        ...task,
                        taskId: task.id,
                        clientToken,
                        createdAt: task.createdAt || Date.now(),
                        updatedAt: Date.now()
                    });
                }
                
                serverTasks.push(task);
            }
        }
        
        // Get updated tasks since last sync
        let updatedTasks = [];
        if (lastSync) {
            const serverUpdated = await tasks.find({
                clientToken,
                updatedAt: { $gt: lastSync }
            });
            updatedTasks = serverUpdated.map(t => ({
                id: t.taskId,
                title: t.title,
                description: t.description,
                created_at: t.createdAt,
                start_time: t.startTime || t.start_time,
                deadline: t.deadline,
                is_completed: t.isCompleted || t.is_completed,
                completed_at: t.completedAt || t.completed_at,
                progress: t.progress,
                priority: t.priority,
                is_public: t.isPublic || t.is_public,
                tags: t.tags
            }));
        }
        
        res.json({
            success: true,
            message: 'Sync completed',
            server_time: Date.now(),
            updated_tasks: updatedTasks
        });
    } catch (error) {
        next(error);
    }
});

/**
 * Get all tasks for a client
 * GET /api/v1/tasks
 */
router.get('/tasks', authenticate, async (req, res, next) => {
    try {
        const clientToken = req.clientToken;
        const clientTasks = await tasks.find({ clientToken });
        
        res.json({
            success: true,
            tasks: clientTasks.map(t => ({
                id: t.taskId,
                title: t.title,
                description: t.description,
                created_at: t.createdAt,
                start_time: t.startTime || t.start_time,
                deadline: t.deadline,
                is_completed: t.isCompleted || t.is_completed,
                completed_at: t.completedAt || t.completed_at,
                progress: t.progress,
                priority: t.priority,
                is_public: t.isPublic || t.is_public,
                tags: t.tags
            }))
        });
    } catch (error) {
        next(error);
    }
});

/**
 * Get profile for a client
 * GET /api/v1/profile
 */
router.get('/profile', authenticate, async (req, res, next) => {
    try {
        const clientToken = req.clientToken;
        const profile = await profiles.findOne({ clientToken });
        const clientTasks = await tasks.find({ clientToken });
        
        // Calculate statistics
        const activeTasks = clientTasks.filter(t => !t.isCompleted && !t.is_completed);
        const completedTasks = clientTasks.filter(t => t.isCompleted || t.is_completed);
        const now = Date.now();
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const todayStart = today.getTime();
        
        const completedToday = completedTasks.filter(t => {
            const completedAt = t.completedAt || t.completed_at;
            return completedAt && completedAt >= todayStart;
        }).length;
        
        res.json({
            success: true,
            profile: profile || {
                displayName: 'Life App User',
                motto: 'Push to Start, Pop to Finish',
                status: 'Available'
            },
            stats: {
                activeTasks: activeTasks.length,
                completedTasks: completedTasks.length,
                completedToday: completedToday,
                totalTasks: clientTasks.length
            }
        });
    } catch (error) {
        next(error);
    }
});

/**
 * Update profile
 * PUT /api/v1/profile
 */
router.put('/profile', authenticate, async (req, res, next) => {
    try {
        const clientToken = req.clientToken;
        const { displayName, motto, status } = req.body;
        
        await profiles.update(
            { clientToken },
            {
                clientToken,
                displayName: displayName || 'Life App User',
                motto: motto || 'Push to Start, Pop to Finish',
                status: status || 'Available',
                updatedAt: Date.now()
            },
            { upsert: true }
        );
        
        res.json({
            success: true,
            message: 'Profile updated'
        });
    } catch (error) {
        next(error);
    }
});

/**
 * Get public data for web dashboard (no auth required)
 * GET /api/v1/public/dashboard
 */
router.get('/public/dashboard', async (req, res, next) => {
    try {
        // Get all profiles
        const allProfiles = await profiles.find({});
        
        // Get public tasks only
        const publicTasks = await tasks.find({
            $or: [
                { isPublic: true },
                { is_public: true }
            ]
        });
        
        // Calculate stats
        const now = Date.now();
        const activeTasks = publicTasks.filter(t => !t.isCompleted && !t.is_completed);
        
        // Group tasks by timeline
        const overdueCount = activeTasks.filter(t => {
            const deadline = t.deadline;
            return deadline && deadline < now;
        }).length;
        
        res.json({
            success: true,
            profiles: allProfiles.map(p => ({
                displayName: p.displayName,
                motto: p.motto,
                status: p.status
            })),
            publicTasks: publicTasks.map(t => ({
                id: t.taskId,
                title: t.title,
                description: t.description,
                deadline: t.deadline,
                priority: t.priority,
                progress: t.progress,
                isCompleted: t.isCompleted || t.is_completed,
                tags: t.tags
            })),
            stats: {
                totalPublicTasks: publicTasks.length,
                activeTasks: activeTasks.length,
                overdueCount: overdueCount
            }
        });
    } catch (error) {
        next(error);
    }
});

/**
 * Get timeline data for a client
 * GET /api/v1/timeline
 */
router.get('/timeline', authenticate, async (req, res, next) => {
    try {
        const clientToken = req.clientToken;
        const clientTasks = await tasks.find({ 
            clientToken,
            $or: [
                { isCompleted: false },
                { is_completed: false },
                { isCompleted: { $exists: false }, is_completed: { $exists: false } }
            ]
        });
        
        const now = Date.now();
        
        // Categorize tasks
        const timeline = {
            overdue: [],
            today: [],
            tomorrow: [],
            thisWeek: [],
            later: []
        };
        
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        const todayEnd = new Date(todayStart);
        todayEnd.setDate(todayEnd.getDate() + 1);
        const tomorrowEnd = new Date(todayStart);
        tomorrowEnd.setDate(tomorrowEnd.getDate() + 2);
        const weekEnd = new Date(todayStart);
        weekEnd.setDate(weekEnd.getDate() + 7);
        
        clientTasks.forEach(t => {
            const task = {
                id: t.taskId,
                title: t.title,
                deadline: t.deadline,
                startTime: t.startTime || t.start_time,
                priority: t.priority,
                progress: t.progress,
                tags: t.tags
            };
            
            const targetTime = t.deadline || t.startTime || t.start_time;
            
            if (!targetTime) {
                timeline.later.push(task);
            } else if (targetTime < now) {
                timeline.overdue.push(task);
            } else if (targetTime < todayEnd.getTime()) {
                timeline.today.push(task);
            } else if (targetTime < tomorrowEnd.getTime()) {
                timeline.tomorrow.push(task);
            } else if (targetTime < weekEnd.getTime()) {
                timeline.thisWeek.push(task);
            } else {
                timeline.later.push(task);
            }
        });
        
        res.json({
            success: true,
            timeline,
            serverTime: now
        });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
