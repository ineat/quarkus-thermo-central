db.auth('admin-user', 'admin-password')

db = db.getSiblingDB('poc-quarkus-db')

db.createUser({
    user: 'quarkus-user',
    pwd: 'quarkus-password',
    roles: [
        {
            role: 'root',
            db: 'admin',
        },
    ],
});

db.createCollection("poc-quarkus-metrics")