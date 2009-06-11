CREATE TABLE job_status (
	job_status_id			TINYINT			UNSIGNED NOT NULL AUTO_INCREMENT,
	job_status_string		VARCHAR(32)		NOT NULL,
	PRIMARY KEY(job_status_id)
);

CREATE TABLE job (
	job_id					INT 			UNSIGNED NOT NULL AUTO_INCREMENT,
	job_resource_key		VARCHAR(32)		NOT NULL,
	job_name				VARCHAR(32) 	NOT NULL,
	job_status_id			INT 			DEFAULT NULL,
	subject_name			VARCHAR(104)	DEFAULT NULL,
	job_description			VARCHAR(104) 	DEFAULT NULL,
	creation_time			DATETIME 		DEFAULT NULL,
	queued_time				DATETIME 		DEFAULT NULL,
	success_flag			TINYINT 		UNSIGNED DEFAULT NULL,
	finished_flag			TINYINT 		UNSIGNED DEFAULT NULL,
	active_time				DATETIME 		DEFAULT NULL,
	worker_terminated_time	DATETIME 		DEFAULT NULL,
	job_all_done_time 		DATETIME 		DEFAULT NULL,
	client_hostname 		VARCHAR(255) 	DEFAULT NULL,
	execution_host 			VARCHAR(255) 	DEFAULT NULL,
	worker_node_host 		VARCHAR(255) 	DEFAULT NULL,
	version 				VARCHAR(16)		DEFAULT NULL,
	files_total 			INT 			UNSIGNED DEFAULT NULL,
	files_transferred 		INT 			UNSIGNED DEFAULT NULL,
	volume_total 			INT 			UNSIGNED DEFAULT NULL,
	volume_transferred 		INT 			UNSIGNED DEFAULT NULL,
	PRIMARY KEY(job_id),
	UNIQUE INDEX job_index_1(job_resource_key),
	INDEX job_index_2(subject_name),
	CONSTRAINT FK_job_status FOREIGN KEY (job_status_id) REFERENCES job_status(job_status_id)
);