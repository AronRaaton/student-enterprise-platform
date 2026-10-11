-- Fresh MySQL 8 baseline generated offline from JPA mappings, October 2026.
-- No private database connection or exported records. Apply ONCE to an empty database.
USE student_enterprise_demo;
SET NAMES utf8mb4;

    create table admin (
       id bigint not null,
        email varchar(255),
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table announcement (
       announcement_id bigint not null auto_increment,
        content TEXT,
        created_time datetime(6),
        feedback varchar(255),
        reviewed_time datetime(6),
        status varchar(255),
        title varchar(255),
        enterprise_id bigint,
        primary key (announcement_id)
    ) engine=InnoDB;

    create table chat_message (
       id integer not null auto_increment,
        content TEXT,
        is_read bit,
        receiver_id varchar(255) not null,
        sender_id varchar(255) not null,
        sender_name varchar(255) not null,
        sender_type varchar(255) not null,
        timestamp datetime(6),
        primary key (id)
    ) engine=InnoDB;

    create table competition (
       competition_id bigint not null auto_increment,
        create_time datetime(6) not null,
        description TEXT,
        end_date date not null,
        max_participants integer,
        registration_deadline date not null,
        requirements TEXT,
        rewards TEXT,
        start_date date not null,
        status varchar(255) not null,
        title varchar(255) not null,
        update_time datetime(6) not null,
        winners_selected bit,
        enterprise_id bigint,
        first_prize_submission_id bigint,
        second_prize_submission_id bigint,
        third_prize_submission_id bigint,
        primary key (competition_id)
    ) engine=InnoDB;

    create table competition_registration (
       registration_id bigint not null auto_increment,
        registration_time datetime(6) not null,
        status varchar(255) not null,
        competition_id bigint,
        student_id bigint,
        primary key (registration_id)
    ) engine=InnoDB;

    create table competition_submission (
       submission_id bigint not null auto_increment,
        content TEXT,
        feedback TEXT,
        file_url varchar(255),
        score decimal(19,2),
        status varchar(255) not null,
        submission_time datetime(6) not null,
        uploaded_file varchar(255),
        registration_id bigint,
        primary key (submission_id)
    ) engine=InnoDB;

    create table enterprise (
       enterprise_id bigint not null auto_increment,
        description varchar(255),
        email varchar(255),
        employee_count integer,
        founding_year integer,
        location varchar(255),
        name varchar(255),
        password varchar(255),
        rating double precision,
        register_date datetime(6),
        type varchar(255),
        website varchar(255),
        primary key (enterprise_id)
    ) engine=InnoDB;

    create table enterprise_event (
       event_id bigint not null auto_increment,
        benefits TEXT,
        create_time datetime(6) not null,
        description TEXT,
        end_time datetime(6) not null,
        event_type varchar(255) not null,
        location varchar(255) not null,
        max_participants integer,
        registration_deadline datetime(6) not null,
        start_time datetime(6) not null,
        status varchar(255) not null,
        title varchar(255) not null,
        update_time datetime(6) not null,
        enterprise_id bigint,
        primary key (event_id)
    ) engine=InnoDB;

    create table enterprise_rating (
       id bigint not null auto_increment,
        comment TEXT,
        create_time datetime(6) not null,
        enterprise_id bigint not null,
        rating integer not null,
        student_id bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table enterprise_verification (
       verification_id bigint not null auto_increment,
        business_license varchar(255),
        legal_representative varchar(255),
        submit_time datetime(6),
        verification_status varchar(255),
        verifier_id bigint,
        verify_time datetime(6),
        enterprise_id bigint,
        primary key (verification_id)
    ) engine=InnoDB;

    create table enterprise_favorite (
       enterprise_id bigint not null,
        student_id bigint not null,
        created_time datetime(6),
        primary key (enterprise_id, student_id)
    ) engine=InnoDB;

    create table event_registration (
       registration_id bigint not null auto_increment,
        registration_time datetime(6) not null,
        status varchar(255) not null,
        event_id bigint,
        student_id bigint,
        primary key (registration_id)
    ) engine=InnoDB;

    create table forum_comment (
       comment_id bigint not null auto_increment,
        author_id varchar(255) not null,
        author_type varchar(255) not null,
        content TEXT not null,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        parent_comment_id bigint,
        post_id bigint not null,
        primary key (comment_id)
    ) engine=InnoDB;

    create table forum_like (
       like_id bigint not null auto_increment,
        created_at datetime(6) not null,
        user_id varchar(255) not null,
        user_type varchar(255) not null,
        post_id bigint not null,
        primary key (like_id)
    ) engine=InnoDB;

    create table forum_post (
       post_id bigint not null auto_increment,
        author_id varchar(255) not null,
        author_type varchar(255) not null,
        category varchar(255),
        content TEXT not null,
        created_at datetime(6) not null,
        is_pinned bit not null,
        like_count integer not null,
        status varchar(255) not null,
        subcategory varchar(255),
        title varchar(255) not null,
        updated_at datetime(6) not null,
        view_count integer not null,
        primary key (post_id)
    ) engine=InnoDB;

    create table job_application (
       application_id bigint not null auto_increment,
        application_time datetime(6) not null,
        feedback TEXT,
        review_time datetime(6),
        status varchar(255) not null,
        job_id bigint,
        student_id bigint,
        primary key (application_id)
    ) engine=InnoDB;

    create table job_posting (
       job_id bigint not null auto_increment,
        city varchar(255) not null,
        description TEXT,
        education_requirement varchar(255),
        job_type varchar(255) not null,
        max_salary integer,
        min_salary integer,
        position_title varchar(255),
        post_time datetime(6) not null,
        requirements TEXT,
        status varchar(255) not null,
        tech_stack TEXT,
        title varchar(255) not null,
        enterprise_id bigint,
        primary key (job_id)
    ) engine=InnoDB;

    create table notice (
       notice_id bigint not null auto_increment,
        content TEXT,
        created_time datetime(6),
        feedback varchar(255),
        reviewed_time datetime(6),
        status varchar(255),
        title varchar(255),
        enterprise_id bigint,
        primary key (notice_id)
    ) engine=InnoDB;

    create table order_training_step (
       id bigint not null auto_increment,
        content TEXT not null,
        file_url varchar(255),
        project_id bigint not null,
        step_number integer not null,
        submission_time datetime(6) not null,
        title varchar(255) not null,
        user_id bigint not null,
        user_role varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table project_application (
       application_id bigint not null auto_increment,
        application_reason TEXT,
        apply_time datetime(6) not null,
        contact_info varchar(255),
        feedback TEXT,
        notes TEXT,
        relevant_experience TEXT,
        review_time datetime(6),
        status varchar(255) not null,
        uploaded_file LONGTEXT,
        project_id bigint,
        student_id bigint,
        primary key (application_id)
    ) engine=InnoDB;

    create table project_collaboration (
       project_id bigint not null auto_increment,
        collaboration_type varchar(255),
        create_time datetime(6) not null,
        current_step integer,
        description TEXT,
        end_date date,
        location varchar(255),
        max_participants integer,
        project_direction varchar(255) not null,
        project_plan TEXT,
        project_plan_file varchar(255),
        required_skills TEXT,
        resources TEXT,
        start_date date,
        status varchar(255) not null,
        support TEXT,
        tech_requirement TEXT,
        time_cycle varchar(255),
        title varchar(255) not null,
        training_plan TEXT,
        enterprise_id bigint,
        primary key (project_id)
    ) engine=InnoDB;

    create table project_step_submission (
       submission_id bigint not null auto_increment,
        batch_number integer not null,
        brief_description varchar(5),
        content TEXT,
        feedback TEXT,
        is_final_submission bit,
        review_time datetime(6),
        status varchar(255) not null,
        step_number integer,
        submission_time datetime(6) not null,
        uploaded_file varchar(255),
        project_id bigint,
        student_id bigint,
        primary key (submission_id)
    ) engine=InnoDB;

    create table student (
       student_id bigint not null auto_increment,
        completed_tasks integer,
        email varchar(100),
        enrollment_year integer,
        major varchar(255),
        name varchar(100) not null,
        password varchar(255),
        portfolio_files TEXT,
        register_date datetime(6),
        resume_file varchar(255),
        school varchar(255),
        student_number varchar(255),
        transcript_file varchar(255),
        primary key (student_id)
    ) engine=InnoDB;

    create table student_favorite_enterprise (
       enterprise_id bigint not null,
        student_id bigint not null,
        primary key (enterprise_id, student_id)
    ) engine=InnoDB;

    create table student_follow_enterprise (
       enterprise_id bigint not null,
        student_id bigint not null,
        primary key (student_id, enterprise_id)
    ) engine=InnoDB;

    create table submission_history (
       history_id bigint not null auto_increment,
        content longtext,
        created_time datetime(6),
        description TEXT,
        feedback TEXT,
        file_url varchar(255),
        review_time datetime(6),
        status varchar(255),
        submission_type varchar(255),
        uploaded_file varchar(255),
        submission_id bigint,
        primary key (history_id)
    ) engine=InnoDB;

    create table task (
       task_id bigint not null auto_increment,
        content TEXT not null,
        create_time datetime(6),
        difficulty integer not null,
        requirements varchar(200) not null,
        status varchar(50),
        summary varchar(200) not null,
        tech_stack varchar(200) not null,
        enterprise_id bigint,
        primary key (task_id)
    ) engine=InnoDB;

    -- Baseline correction for the original dual mapping (TaskAssignment entity + Task.assignedStudents).
    -- Keep a generated entity ID and uniqueness of the task/student pair.
    create table task_assignment (
        id bigint not null auto_increment,
        student_id bigint not null,
        task_id bigint not null,
        primary key (id),
        constraint uk_task_assignment_pair unique (task_id, student_id)
    ) engine=InnoDB;

    create table task_rating (
       id bigint not null auto_increment,
        comment TEXT,
        create_time datetime(6) not null,
        rating integer not null,
        student_id bigint not null,
        task_id bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table task_submission (
       submission_id bigint not null auto_increment,
        feedback TEXT,
        file_url varchar(255),
        rating integer,
        review_time datetime(6),
        status varchar(255),
        submission_content TEXT,
        submission_count integer,
        submission_time datetime(6),
        uploaded_file varchar(255),
        student_id bigint not null,
        task_id bigint not null,
        primary key (submission_id)
    ) engine=InnoDB;

    create table technical_challenge (
       challenge_id bigint not null auto_increment,
        create_time datetime(6) not null,
        deadline date,
        description TEXT,
        difficulty integer,
        required_skills TEXT,
        reward_description TEXT,
        status varchar(255) not null,
        title varchar(255) not null,
        enterprise_id bigint,
        primary key (challenge_id)
    ) engine=InnoDB;

    create table technical_challenge_submission (
       submission_id bigint not null auto_increment,
        feedback TEXT,
        rating integer,
        review_time datetime(6),
        solution_description TEXT,
        solution_url varchar(255),
        status varchar(255) not null,
        submit_time datetime(6) not null,
        uploaded_file varchar(255),
        challenge_id bigint,
        student_id bigint,
        primary key (submission_id)
    ) engine=InnoDB;

    create table training_chapter (
       chapter_id bigint not null auto_increment,
        content TEXT,
        description TEXT,
        material_id bigint not null,
        sequence integer not null,
        title varchar(255) not null,
        primary key (chapter_id)
    ) engine=InnoDB;

    create table training_file (
       file_id bigint not null auto_increment,
        chapter_id bigint not null,
        file_name varchar(255) not null,
        file_path varchar(255) not null,
        file_type varchar(255) not null,
        upload_time datetime(6) not null,
        primary key (file_id)
    ) engine=InnoDB;

    create table training_material (
       material_id bigint not null auto_increment,
        create_time datetime(6) not null,
        description TEXT,
        enterprise_id bigint not null,
        industry_type varchar(255) not null,
        status varchar(255) not null,
        title varchar(255) not null,
        update_time datetime(6) not null,
        primary key (material_id)
    ) engine=InnoDB;

    create table training_report (
       id bigint not null auto_increment,
        content TEXT not null,
        project_id bigint not null,
        student_id bigint not null,
        submission_time datetime(6) not null,
        title varchar(255) not null,
        uploaded_file varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table enterprise 
       add constraint UK_lxkpjygh71r527lxnmgv1t0y6 unique (email);

    alter table student 
       add constraint UK_fe0i52si7ybu0wjedj6motiim unique (email);

    alter table announcement 
       add constraint FK5w23l8iiwt5rpvnx7gsaary8w 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table competition 
       add constraint FK7r00kd0od2hxpfcpifc1v3sq6 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table competition 
       add constraint FK2fj83yia9dr6yw53nf4ooxinf 
       foreign key (first_prize_submission_id) 
       references competition_submission (submission_id);

    alter table competition 
       add constraint FK2xffctrbpalks01c0qjb2r6ee 
       foreign key (second_prize_submission_id) 
       references competition_submission (submission_id);

    alter table competition 
       add constraint FK160ufbxx8aehe6qkb773q44rv 
       foreign key (third_prize_submission_id) 
       references competition_submission (submission_id);

    alter table competition_registration 
       add constraint FKesqydrsoc73ipkw9hqx5ma7uk 
       foreign key (competition_id) 
       references competition (competition_id);

    alter table competition_registration 
       add constraint FKll12hf9qkkmrgyujurohuql1h 
       foreign key (student_id) 
       references student (student_id);

    alter table competition_submission 
       add constraint FKn5ih86ogdy7ligp2flhjccvp8 
       foreign key (registration_id) 
       references competition_registration (registration_id);

    alter table enterprise_event 
       add constraint FK78bpq9nv77b2c9in7tk9x5p31 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table enterprise_verification 
       add constraint FKippfo5jw1x2dkn2psbt6k7skq 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table enterprise_favorite 
       add constraint FKedbkxfp72b5mp77mho1tdb3ww 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table enterprise_favorite 
       add constraint FKkgwlefdni81h5genqv0rq4cid 
       foreign key (student_id) 
       references student (student_id);

    alter table event_registration 
       add constraint FKqwpbi45lv8gynsgncekg1rkhs 
       foreign key (event_id) 
       references enterprise_event (event_id);

    alter table event_registration 
       add constraint FKgcbwgm52hfd8th3j5y44udyig 
       foreign key (student_id) 
       references student (student_id);

    alter table forum_comment 
       add constraint FK6qls9p7fqp1hwlcrcv4oicewk 
       foreign key (parent_comment_id) 
       references forum_comment (comment_id);

    alter table forum_comment 
       add constraint FKcd6dtk9l07gt6pdngutp0cx53 
       foreign key (post_id) 
       references forum_post (post_id);

    alter table forum_like 
       add constraint FK25cun0muob2k5jo6v34rod3st 
       foreign key (post_id) 
       references forum_post (post_id);

    alter table job_application 
       add constraint FKobel3nnbi451ftywx9q9m2qax 
       foreign key (job_id) 
       references job_posting (job_id);

    alter table job_application 
       add constraint FKm235erig14wtrwxkg73j2j3oe 
       foreign key (student_id) 
       references student (student_id);

    alter table job_posting 
       add constraint FKlnb7j2eipc5pv5mxyu47hl88s 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table notice 
       add constraint FK14t3nqry4639s4xgg49ifeig1 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table project_application 
       add constraint FKe3m3uhworq06i1yy3yxru060t 
       foreign key (project_id) 
       references project_collaboration (project_id);

    alter table project_application 
       add constraint FKhk87q7hbrrtnurxvs042alaix 
       foreign key (student_id) 
       references student (student_id);

    alter table project_collaboration 
       add constraint FKniqfotou64vfyeexy72bwnag1 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table project_step_submission 
       add constraint FKkqdupa0d8cyexufjrfe7ik281 
       foreign key (project_id) 
       references project_collaboration (project_id);

    alter table project_step_submission 
       add constraint FKprh2aq1qsf6ufgswtmb7or0gt 
       foreign key (student_id) 
       references student (student_id);

    alter table student_follow_enterprise 
       add constraint FKqd1caa0xco0jtkjmqsp3u8f4q 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table student_follow_enterprise 
       add constraint FKeg0dv27a8yjt67l2t82pocdkr 
       foreign key (student_id) 
       references student (student_id);

    alter table submission_history 
       add constraint FK3v93ror4vph8jsisyc7d52jif 
       foreign key (submission_id) 
       references task_submission (submission_id);

    alter table task 
       add constraint FKa0hub617ifw7bmeq383cr9731 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table task_assignment 
       add constraint FKn98beg8k5yan3bqrgn88y19h2 
       foreign key (student_id) 
       references student (student_id);

    alter table task_assignment 
       add constraint FKjec38nl7lfmlgdn036w0h3hbt 
       foreign key (task_id) 
       references task (task_id);

    alter table task_submission 
       add constraint FK77tgtjebeuout9npjpridwjtl 
       foreign key (student_id) 
       references student (student_id);

    alter table task_submission 
       add constraint FKjlw5bs08e3fwrvvtkxnnv24rr 
       foreign key (task_id) 
       references task (task_id);

    alter table technical_challenge 
       add constraint FKf38rhnvjc2ppjsd2hgdnoprrl 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

    alter table technical_challenge_submission 
       add constraint FK68anvntvt9h69rrf06luekrrq 
       foreign key (challenge_id) 
       references technical_challenge (challenge_id);

    alter table technical_challenge_submission 
       add constraint FKc42cv2g96cy3itah7kpdt6mjp 
       foreign key (student_id) 
       references student (student_id);

    alter table training_chapter 
       add constraint FKrqsmtw6pnl2dchu5nk9n2t37x 
       foreign key (material_id) 
       references training_material (material_id);

    alter table training_file 
       add constraint FK6roened6ql7xvvc6gt92fu02 
       foreign key (chapter_id) 
       references training_chapter (chapter_id);

    alter table training_material 
       add constraint FKfi5ls54po1fxbfvv8mccvyp84 
       foreign key (enterprise_id) 
       references enterprise (enterprise_id);

-- Constraints/indexes retained from the original V17/V21/chat schema designs.
-- These do not solve concurrent counter updates or application capacity races.
ALTER TABLE project_application ADD CONSTRAINT uk_project_student UNIQUE (project_id, student_id);
ALTER TABLE forum_like ADD CONSTRAINT uk_forum_like_user_post UNIQUE (post_id, user_id, user_type);
CREATE INDEX idx_chat_message_sender_id ON chat_message (sender_id);
CREATE INDEX idx_chat_message_receiver_id ON chat_message (receiver_id);
CREATE INDEX idx_chat_message_is_read ON chat_message (is_read);
CREATE INDEX idx_project_enterprise_id ON project_collaboration (enterprise_id);
CREATE INDEX idx_project_status ON project_collaboration (status);
CREATE INDEX idx_application_status ON project_application (status);
CREATE INDEX idx_forum_post_author ON forum_post (author_id, author_type);
CREATE INDEX idx_forum_post_created_at ON forum_post (created_at);
