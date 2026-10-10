-- Inicialización de bases de datos para arquitectura de microservicios (Database-per-service)
SELECT 'CREATE DATABASE erp_auth_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'erp_auth_db')\gexec

SELECT 'CREATE DATABASE erp_orders_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'erp_orders_db')\gexec

GRANT ALL PRIVILEGES ON DATABASE erp_auth_db TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_orders_db TO erp_user;
