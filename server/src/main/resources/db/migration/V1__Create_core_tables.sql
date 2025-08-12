-- Create core tables for Boardify application

-- Companies table
CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table with inheritance
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_type VARCHAR(10) NOT NULL -- Discriminator column
);

-- HR Users table
CREATE TABLE hr_manager (
    id UUID PRIMARY KEY REFERENCES users(id),
    company_id INTEGER NOT NULL REFERENCES companies(id)
);

-- Departments table
CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    company_id INTEGER NOT NULL REFERENCES companies(id),
    hr_manager_id UUID NOT NULL REFERENCES hr_manager(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, company_id)
);

-- Hire Users table
CREATE TABLE hire_user (
    id UUID PRIMARY KEY REFERENCES users(id),
    gender VARCHAR(20),
    title VARCHAR(255),
    picture_url TEXT,
    hr_id UUID NOT NULL REFERENCES hr_manager(id),
    department_id INTEGER REFERENCES departments(id)
);

-- Templates table
CREATE TABLE templates (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    hr_id UUID NOT NULL REFERENCES hr_manager(id),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tasks table
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    task_type VARCHAR(20) NOT NULL,
    requires_signature BOOLEAN NOT NULL DEFAULT FALSE,
    resource_url TEXT,
    event_date TIMESTAMP,
    priority VARCHAR(20),
    estimated_hours DECIMAL(5,2),
    order_index INTEGER,
    template_id INTEGER NOT NULL REFERENCES templates(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Todos table
CREATE TABLE todos (
    id SERIAL PRIMARY KEY,
    hire_id UUID NOT NULL REFERENCES hire_user(id),
    task_id INTEGER NOT NULL REFERENCES tasks(id),
    template_id INTEGER NOT NULL REFERENCES templates(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    completed_at TIMESTAMP,
    due_date TIMESTAMP,
    reminder_sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Progress table
CREATE TABLE progress (
    id SERIAL PRIMARY KEY,
    hire_id UUID NOT NULL REFERENCES hire_user(id),
    template_id INTEGER NOT NULL REFERENCES templates(id),
    total_tasks INTEGER NOT NULL DEFAULT 0,
    completed_tasks INTEGER NOT NULL DEFAULT 0,
    completion_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(hire_id, template_id)
);

-- Department Templates junction table
CREATE TABLE department_templates (
    department_id INTEGER NOT NULL REFERENCES departments(id),
    template_id INTEGER NOT NULL REFERENCES templates(id),
    PRIMARY KEY (department_id, template_id)
);

-- Company Subscriptions table
CREATE TABLE company_subs (
    id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(id),
    subscription_plan VARCHAR(20) NOT NULL DEFAULT 'FREE',
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Documents table
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    requires_signature BOOLEAN DEFAULT FALSE,
    signature_status VARCHAR(20),
    task_id INTEGER REFERENCES tasks(id),
    todo_id INTEGER REFERENCES todos(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications table
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    recipient_id UUID NOT NULL REFERENCES hr_manager(id),
    related_todo_id INTEGER REFERENCES todos(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);