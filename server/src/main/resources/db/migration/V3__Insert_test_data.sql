-- Insert test data for development and testing

-- Insert test companies
INSERT INTO companies (id, name) VALUES 
(1, 'TechCorp Solutions'),
(2, 'InnovateLabs Inc'),
(3, 'StartupHub LLC');

-- Insert test HR users
INSERT INTO users (id, email, password, first_name, last_name, role, user_type) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'hr1@techcorp.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5gtOtMkwVG6.6TO8.S5e', 'Sarah', 'Johnson', 'HR_MANAGER', 'HR'),
('550e8400-e29b-41d4-a716-446655440002', 'hr2@innovatelabs.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5gtOtMkwVG6.6TO8.S5e', 'Michael', 'Chen', 'HR_MANAGER', 'HR'),
('550e8400-e29b-41d4-a716-446655440003', 'hr3@startuphub.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5gtOtMkwVG6.6TO8.S5e', 'Emily', 'Rodriguez', 'HR_MANAGER', 'HR');

INSERT INTO hr_manager (id, company_id) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 1),
('550e8400-e29b-41d4-a716-446655440002', 2),
('550e8400-e29b-41d4-a716-446655440003', 3);

-- Insert test departments
INSERT INTO departments (id, name, company_id, hr_manager_id) VALUES 
(1, 'Engineering', 1, '550e8400-e29b-41d4-a716-446655440001'),
(2, 'Marketing', 1, '550e8400-e29b-41d4-a716-446655440001'),
(3, 'Sales', 1, '550e8400-e29b-41d4-a716-446655440001'),
(4, 'Product', 2, '550e8400-e29b-41d4-a716-446655440002'),
(5, 'Design', 2, '550e8400-e29b-41d4-a716-446655440002'),
(6, 'Operations', 3, '550e8400-e29b-41d4-a716-446655440003');

-- Insert test hire users
INSERT INTO users (id, email, password, first_name, last_name, role, user_type) VALUES 
('550e8400-e29b-41d4-a716-446655440011', 'john.doe@techcorp.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5gtOtMkwVG6.6TO8.S5e', 'John', 'Doe', 'NEW_HIRE', 'HIRE'),
('550e8400-e29b-41d4-a716-446655440012', 'jane.smith@techcorp.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5gtOtMkwVG6.6TO8.S5e', 'Jane', 'Smith', 'NEW_HIRE', 'HIRE'),
('550e8400-e29b-41d4-a716-446655440013', 'alex.wilson@innovatelabs.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5gtOtMkwVG6.6TO8.S5e', 'Alex', 'Wilson', 'NEW_HIRE', 'HIRE'),
('550e8400-e29b-41d4-a716-446655440014', 'lisa.brown@startuphub.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5gtOtMkwVG6.6TO8.S5e', 'Lisa', 'Brown', 'NEW_HIRE', 'HIRE');

INSERT INTO hire_user (id, title, hr_id, department_id) VALUES 
('550e8400-e29b-41d4-a716-446655440011', 'Software Engineer', '550e8400-e29b-41d4-a716-446655440001', 1),
('550e8400-e29b-41d4-a716-446655440012', 'Marketing Specialist', '550e8400-e29b-41d4-a716-446655440001', 2),
('550e8400-e29b-41d4-a716-446655440013', 'Product Manager', '550e8400-e29b-41d4-a716-446655440002', 4),
('550e8400-e29b-41d4-a716-446655440014', 'Operations Coordinator', '550e8400-e29b-41d4-a716-446655440003', 6);

-- Insert test templates
INSERT INTO templates (id, title, description, status, hr_id) VALUES 
(1, 'Software Engineer Onboarding', 'Complete onboarding process for new software engineers', 'PENDING', '550e8400-e29b-41d4-a716-446655440001'),
(2, 'Marketing Team Onboarding', 'Onboarding process for marketing team members', 'PENDING', '550e8400-e29b-41d4-a716-446655440001'),
(3, 'Product Manager Onboarding', 'Comprehensive onboarding for product managers', 'PENDING', '550e8400-e29b-41d4-a716-446655440002'),
(4, 'General Employee Onboarding', 'Standard onboarding for all new employees', 'PENDING', '550e8400-e29b-41d4-a716-446655440003');

-- Insert test tasks
INSERT INTO tasks (id, title, description, task_type, requires_signature, order_index, template_id, status) VALUES 
-- Software Engineer Template Tasks
(1, 'Complete Employee Handbook', 'Read and acknowledge the employee handbook', 'DOCUMENT', true, 1, 1, 'PENDING'),
(2, 'Setup Development Environment', 'Install required development tools and access repositories', 'RESOURCE', false, 2, 1, 'PENDING'),
(3, 'Team Introduction Meeting', 'Meet with your team lead and colleagues', 'EVENT', false, 3, 1, 'PENDING'),
(4, 'Security Training', 'Complete mandatory security training course', 'DOCUMENT', true, 4, 1, 'PENDING'),
(5, 'First Code Review', 'Submit your first code for review', 'RESOURCE', false, 5, 1, 'PENDING'),

-- Marketing Template Tasks
(6, 'Brand Guidelines Review', 'Study company brand guidelines and style guide', 'DOCUMENT', false, 1, 2, 'PENDING'),
(7, 'Marketing Tools Access', 'Get access to marketing automation tools', 'RESOURCE', false, 2, 2, 'PENDING'),
(8, 'Campaign Strategy Session', 'Attend campaign planning meeting', 'EVENT', false, 3, 2, 'PENDING'),

-- Product Manager Template Tasks
(9, 'Product Roadmap Review', 'Review current product roadmap and priorities', 'DOCUMENT', false, 1, 3, 'PENDING'),
(10, 'Stakeholder Introductions', 'Meet with key stakeholders across departments', 'EVENT', false, 2, 3, 'PENDING'),
(11, 'User Research Training', 'Complete user research methodology training', 'RESOURCE', false, 3, 3, 'PENDING'),

-- General Template Tasks
(12, 'HR Orientation', 'Complete general HR orientation session', 'EVENT', false, 1, 4, 'PENDING'),
(13, 'Benefits Enrollment', 'Enroll in company benefits program', 'DOCUMENT', true, 2, 4, 'PENDING'),
(14, 'Office Tour', 'Take a tour of the office facilities', 'EVENT', false, 3, 4, 'PENDING');

-- Link departments to templates
INSERT INTO department_templates (department_id, template_id) VALUES 
(1, 1), -- Engineering -> Software Engineer Onboarding
(2, 2), -- Marketing -> Marketing Team Onboarding
(4, 3), -- Product -> Product Manager Onboarding
(3, 4), -- Sales -> General Employee Onboarding
(5, 4), -- Design -> General Employee Onboarding
(6, 4); -- Operations -> General Employee Onboarding

-- Insert company subscriptions
INSERT INTO company_subs (company_id, subscription_plan, is_active) VALUES 
(1, 'PREMIUM', true),
(2, 'STANDARD', true),
(3, 'FREE', true);

-- Insert some sample todos (simulating template assignments)
INSERT INTO todos (hire_id, task_id, template_id, status, due_date) VALUES 
-- John Doe (Software Engineer) todos
('550e8400-e29b-41d4-a716-446655440011', 1, 1, 'COMPLETED', CURRENT_TIMESTAMP + INTERVAL '7 days'),
('550e8400-e29b-41d4-a716-446655440011', 2, 1, 'IN_PROGRESS', CURRENT_TIMESTAMP + INTERVAL '3 days'),
('550e8400-e29b-41d4-a716-446655440011', 3, 1, 'PENDING', CURRENT_TIMESTAMP + INTERVAL '5 days'),
('550e8400-e29b-41d4-a716-446655440011', 4, 1, 'PENDING', CURRENT_TIMESTAMP + INTERVAL '10 days'),
('550e8400-e29b-41d4-a716-446655440011', 5, 1, 'PENDING', CURRENT_TIMESTAMP + INTERVAL '14 days'),

-- Jane Smith (Marketing) todos
('550e8400-e29b-41d4-a716-446655440012', 6, 2, 'COMPLETED', CURRENT_TIMESTAMP + INTERVAL '7 days'),
('550e8400-e29b-41d4-a716-446655440012', 7, 2, 'COMPLETED', CURRENT_TIMESTAMP + INTERVAL '3 days'),
('550e8400-e29b-41d4-a716-446655440012', 8, 2, 'IN_PROGRESS', CURRENT_TIMESTAMP + INTERVAL '5 days');

-- Insert progress records
INSERT INTO progress (hire_id, template_id, total_tasks, completed_tasks, completion_percentage) VALUES 
('550e8400-e29b-41d4-a716-446655440011', 1, 5, 1, 20.0),
('550e8400-e29b-41d4-a716-446655440012', 2, 3, 2, 66.67),
('550e8400-e29b-41d4-a716-446655440013', 3, 3, 0, 0.0),
('550e8400-e29b-41d4-a716-446655440014', 4, 3, 0, 0.0);

-- Insert sample notifications
INSERT INTO notifications (type, title, message, recipient_id, related_todo_id) VALUES 
('SIGNATURE_REQUEST', 'Document Signature Required', 'John Doe has completed the Employee Handbook and requires your approval', '550e8400-e29b-41d4-a716-446655440001', 1),
('REMINDER', 'Overdue Task Reminder', 'Jane Smith has an overdue task in her onboarding checklist', '550e8400-e29b-41d4-a716-446655440001', 8);