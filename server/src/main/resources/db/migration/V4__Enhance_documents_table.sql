-- Add new columns to documents table for enhanced functionality
ALTER TABLE documents 
ADD COLUMN IF NOT EXISTS todo_id INTEGER REFERENCES todos(id),
ADD COLUMN IF NOT EXISTS file_size BIGINT,
ADD COLUMN IF NOT EXISTS content_type VARCHAR(255);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_documents_todo_id ON documents(todo_id);
CREATE INDEX IF NOT EXISTS idx_documents_task_template ON documents(task_id);
CREATE INDEX IF NOT EXISTS idx_documents_file_size ON documents(file_size);

-- Add constraint to ensure either task_id or todo_id is present
-- (This is handled at application level for flexibility)