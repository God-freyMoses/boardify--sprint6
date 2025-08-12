-- Create indexes for better performance

-- User-related indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_type ON users(user_type);

-- Company-related indexes
CREATE INDEX idx_companies_name ON companies(name);
CREATE INDEX idx_hr_manager_company ON hr_manager(company_id);
CREATE INDEX idx_hire_user_hr ON hire_user(hr_id);
CREATE INDEX idx_hire_user_department ON hire_user(department_id);

-- Department indexes
CREATE INDEX idx_departments_company ON departments(company_id);
CREATE INDEX idx_departments_hr ON departments(hr_manager_id);
CREATE INDEX idx_departments_name_company ON departments(name, company_id);

-- Template-related indexes
CREATE INDEX idx_templates_hr ON templates(hr_id);
CREATE INDEX idx_templates_status ON templates(status);
CREATE INDEX idx_templates_created_date ON templates(created_date);

-- Task-related indexes
CREATE INDEX idx_tasks_template ON tasks(template_id);
CREATE INDEX idx_tasks_type ON tasks(task_type);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_order ON tasks(template_id, order_index);
CREATE INDEX idx_tasks_signature ON tasks(requires_signature);
CREATE INDEX idx_tasks_event_date ON tasks(event_date);

-- Todo-related indexes
CREATE INDEX idx_todos_hire ON todos(hire_id);
CREATE INDEX idx_todos_task ON todos(task_id);
CREATE INDEX idx_todos_template ON todos(template_id);
CREATE INDEX idx_todos_status ON todos(status);
CREATE INDEX idx_todos_hire_status ON todos(hire_id, status);
CREATE INDEX idx_todos_due_date ON todos(due_date);
CREATE INDEX idx_todos_created_at ON todos(created_at);

-- Progress indexes
CREATE INDEX idx_progress_hire ON progress(hire_id);
CREATE INDEX idx_progress_template ON progress(template_id);
CREATE INDEX idx_progress_completion ON progress(completion_percentage);
CREATE INDEX idx_progress_last_updated ON progress(last_updated);

-- Document indexes
CREATE INDEX idx_documents_task ON documents(task_id);
CREATE INDEX idx_documents_todo ON documents(todo_id);
CREATE INDEX idx_documents_signature ON documents(requires_signature);
CREATE INDEX idx_documents_signature_status ON documents(signature_status);

-- Notification indexes
CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_todo ON notifications(related_todo_id);