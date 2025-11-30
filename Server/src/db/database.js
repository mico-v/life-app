const Datastore = require('nedb-promises');
const path = require('path');

const dataDir = process.env.DATA_DIR || './data';

// Tasks database
const tasks = Datastore.create({
    filename: path.join(dataDir, 'tasks.db'),
    autoload: true
});

// Clients database (stores client tokens and their data)
const clients = Datastore.create({
    filename: path.join(dataDir, 'clients.db'),
    autoload: true
});

// User profiles database
const profiles = Datastore.create({
    filename: path.join(dataDir, 'profiles.db'),
    autoload: true
});

// Create indexes
tasks.ensureIndex({ fieldName: 'clientToken' });
tasks.ensureIndex({ fieldName: 'taskId' });
clients.ensureIndex({ fieldName: 'clientToken', unique: true });
profiles.ensureIndex({ fieldName: 'clientToken', unique: true });

module.exports = {
    tasks,
    clients,
    profiles
};
