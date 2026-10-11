-- Entirely fictional, authored for the October 2026 portfolio edition.
-- Run ONCE against the empty database after 01-schema.sql.
-- Public demo-only application password: DemoOnly-2026! (never a database password).
-- Non-overlapping student/enterprise IDs avoid the known original messaging ID collision in this fixture.
USE student_enterprise_demo;
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO admin (id,email,password) VALUES (9001,'admin@example.invalid','DemoOnly-2026!');
INSERT INTO student (student_id,name,email,password,school,student_number,major,enrollment_year,completed_tasks,register_date)
VALUES (1001,'Demo Student A','student.a@example.invalid','DemoOnly-2026!','Fictional Demo University','DEMO-1001','Software Engineering',2021,1,'2026-10-01 09:00:00'),
(1002,'Demo Student B','student.b@example.invalid','DemoOnly-2026!','Fictional Demo University','DEMO-1002','Computer Science',2022,0,'2026-10-01 09:00:00');
INSERT INTO enterprise (enterprise_id,name,email,password,type,location,employee_count,founding_year,description,rating,register_date,website)
VALUES (2001,'Example Collaboration Lab','enterprise@example.invalid','DemoOnly-2026!','Software','Demo City',10,2020,'Fictional organization for local demonstrations only.',0,'2026-10-01 09:00:00','https://example.invalid');
INSERT INTO enterprise_verification (verification_id,enterprise_id,business_license,legal_representative,verification_status,submit_time,verify_time,verifier_id)
VALUES (1,2001,'','Fictional Representative','APPROVED','2026-10-01 09:00:00','2026-10-01 10:00:00',9001);
INSERT INTO enterprise_favorite (enterprise_id,student_id,created_time) VALUES (2001,1001,'2026-10-01 10:00:00');
INSERT INTO task (task_id,enterprise_id,summary,content,tech_stack,difficulty,create_time,requirements,status)
VALUES (1,2001,'Demo API exercise','Discuss the design of a small collaboration API.','Java, Spring Boot, MySQL',2,'2026-10-01 10:00:00','Use fictional data only.','OPEN');
INSERT INTO task_submission (submission_id,task_id,student_id,submission_content,submission_time,status,submission_count,feedback)
VALUES (1,1,1001,'Fictional completed submission for the contact-list demonstration.','2026-10-02 10:00:00','COMPLETED',1,'Demo feedback.');
INSERT INTO project_collaboration (project_id,enterprise_id,title,description,project_direction,required_skills,max_participants,start_date,end_date,status,create_time,collaboration_type,current_step,location)
VALUES (1,2001,'Demo Collaboration Project','A fictional project for demonstrating applications and reviews.','Backend Development','Java, SQL',3,'2026-10-01','2027-06-30','OPEN','2026-10-01 10:00:00','产学研合作',1,'Demo City');
INSERT INTO project_application (application_id,project_id,student_id,application_reason,status,apply_time,relevant_experience,contact_info)
VALUES (1,1,1001,'I would like to practice backend API development.','PENDING','2026-10-03 10:00:00','Fictional classroom project.','student.a@example.invalid');
INSERT INTO forum_post (post_id,title,content,author_id,author_type,created_at,updated_at,view_count,like_count,is_pinned,status,category,subcategory)
VALUES (1,'Welcome to the demo forum','Discuss collaboration ideas using fictional data.','2001','ENTERPRISE','2026-10-03 09:00:00','2026-10-03 09:00:00',0,1,0,'ACTIVE','COMMUNITY','QA');
INSERT INTO forum_comment (comment_id,post_id,content,author_id,author_type,created_at,updated_at,parent_comment_id)
VALUES (1,1,'How does the application review work?','1001','STUDENT','2026-10-03 10:00:00','2026-10-03 10:00:00',NULL),
(2,1,'Submit an application; the enterprise can approve or reject it.','2001','ENTERPRISE','2026-10-03 11:00:00','2026-10-03 11:00:00',1);
INSERT INTO forum_like (like_id,post_id,user_id,user_type,created_at) VALUES (1,1,'1001','STUDENT','2026-10-03 10:00:00');
INSERT INTO chat_message (id,sender_id,sender_name,sender_type,receiver_id,content,timestamp,is_read)
VALUES (1,'1001','Demo Student A','STUDENT','2001','Hello, I submitted a demo project application.','2026-10-03 12:00:00',1),
(2,'2001','Example Collaboration Lab','ENTERPRISE','1001','Thanks. We will review the fictional application.','2026-10-03 12:01:00',0);
COMMIT;
