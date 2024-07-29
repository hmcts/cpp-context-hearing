# !/bin/bash
#
#####################################################################################################################
# Author: Shyam                                                                               Date: 23/02/2022      #
# Description: This script procures the Event IDs from EventStore and restores into ViewStore for fixing duplicate  #
#              reporting restrictions.                                                                              #
#                                                                                                                   #
# V1.0    2022-02-20    Shyam     Original version                                                                  #
# v1.1    2022-02-25    Arcadius Ahouansou: Added union and delete stetements                                       #
#####################################################################################################################

# Retrieve password from Vault for Production

# Create an ARRAY with 3 elements, hostname, DBname, username

#export SELECTIVE_CATCHUP_DB_CONN="host=psf-dev-ccm02-hearing.postgres.database.azure.com port=5432 dbname=hearingviewstore user=hearing password=hearing sslmode=require"
export SELECTIVE_CATCHUP_DB_CONN="postgresql://hearing:hearing@localhost/hearingviewstore"
psql -v ON_ERROR_STOP=1 ${SELECTIVE_CATCHUP_DB_CONN} <<EOF
   \echo '===Copying streamids to csv file'
   
   \COPY (select distinct hearing_id as stream_id from ( select hearing_id, count(1) , offence_id, label, judicial_result_id from ha_reporting_restriction group by hearing_id, offence_id , label, judicial_result_id having count(1) >1 ) as distinctSteam) TO '/tmp/streamids.csv' DELIMITER ',' CSV HEADER;

   \echo '===Connecting to eventstore'
   \c hearingeventstore
   
   \echo '===Dropping tmp_stream_id'
   DROP TABLE IF EXISTS tmp_stream_id CASCADE;
   
   \echo '===Creating tmp_stream_id'
   CREATE TABLE tmp_stream_id( stream_id uuid primary key);

   \echo '===Loading data from streamids.csv'
   \COPY tmp_stream_id FROM '/tmp/streamids.csv' CSV header;

   \echo '===Copying data into eventids_streamid.csv'
   \COPY (SELECT el.id, el.stream_id FROM event_log el, tmp_stream_id tsid WHERE el.stream_id = tsid.stream_id) TO '/tmp/eventids_streamid.csv' DELIMITER ',' CSV HEADER;

   \echo '===Clearing tmp_stream_id'
   DROP TABLE IF EXISTS tmp_stream_id CASCADE;

   \echo '===Connecting to viewstore'
   \c hearingviewstore
   
   \echo '===Dropping tmp_selective_cu_stream'
   DROP TABLE IF EXISTS tmp_selective_cu_stream CASCADE;
   
   \echo '===Creating tmp_selective_cu_stream'
   CREATE TABLE tmp_selective_cu_stream(event_id uuid primary key, stream_id uuid not null);

   \echo '===Loading data into tmp_selective_cu_stream from eventids_streamid.csv'
   \COPY tmp_selective_cu_stream FROM '/tmp/eventids_streamid.csv' CSV header;

-- EXECUTE DELETE STATEMENT HERE


\echo '=== delete stream data from stream_status'

 delete
from
	stream_status
where
	component = 'EVENT_LISTENER'
	and stream_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);

\echo '=== delete stream data from stream_buffer'
delete
from
	stream_buffer
where
	component = 'EVENT_LISTENER'
	and stream_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	
\echo '=== delete from processed_event using event id'
 delete
from
	processed_event
where
	component = 'EVENT_LISTENER'
	and event_id in (
	select
		event_id
	from
		tmp_selective_cu_stream);


	
\echo '=== delete stream data from application_draft_result'
 delete
from
	application_draft_result
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
		
\echo '=== delete stream data from ha_associated_person'	
 delete
from
	ha_associated_person
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
		
		
\echo '=== delete from ha_case_marker'			
 delete
from
	ha_case_marker
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);


\echo '=== delete from ha_defendant_attendance'			
 delete
from
	ha_defendant_attendance
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);


\echo '=== delete from ha_defendant_case'			
 delete
from
	ha_defendant_case
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);


\echo '=== delete from ha_defendant_referral_reason'			
 delete
from
	ha_defendant_referral_reason
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);




\echo '=== delete from ha_defendant_witnesses'			
 delete
from
	ha_defendant_witnesses
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);


\echo '=== delete from ha_draft_result'			
 delete
from
	ha_draft_result
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);


\echo '=== delete from ha_hearing_applicant_counsel'			
 delete
from
	ha_hearing_applicant_counsel
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);

\echo '=== delete from ha_hearing_application'			
 delete
from
	ha_hearing_application
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
		
\echo '=== delete from ha_hearing_case_note'			
 delete
from
	ha_hearing_case_note
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
		

\echo '=== delete from ha_hearing_company_representative'			
 delete
from
	ha_hearing_company_representative
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
				

\echo '=== delete from ha_hearing_day'			
 delete
from
	ha_hearing_day
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	
\echo '=== delete from ha_hearing_defence_counsel'			
 delete
from
	ha_hearing_defence_counsel
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	

\echo '=== delete from ha_hearing_event'			
 delete
from
	ha_hearing_event
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);


\echo '=== delete from ha_hearing_interpreter_intermediary'			
 delete
from
	ha_hearing_interpreter_intermediary
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	

\echo '=== delete from ha_hearing_prosecution_counsel'			
 delete
from
	ha_hearing_prosecution_counsel
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	

\echo '=== delete from ha_hearing_youth_court_defendants'			
 delete
from
	ha_hearing_youth_court_defendants
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	

\echo '=== delete from ha_judicial_role'			
 delete
from
	ha_judicial_role
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	
\echo '=== delete from ha_now'			
 delete
from
	ha_now
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	


\echo '=== delete from ha_reporting_restriction'			
 delete
from
	ha_reporting_restriction
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	

\echo '=== delete from ha_request_approval'			
 delete
from
	ha_request_approval
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	



\echo '=== delete from ha_target'			
 delete
from
	ha_target
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	

\echo '=== delete from ha_witness'			
 delete
from
	ha_witness
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	


\echo '=== delete from nows'			
 delete
from
	nows
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	

\echo '=== delete from result_line'			
 delete
from
	result_line
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);

\echo '=== delete from ha_offence'			
 delete
from
	ha_offence
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);
	
		
\echo '=== delete from ha_defendant'			
 delete
from
	ha_defendant
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);

		

\echo '=== delete from ha_case'	
 delete
from
	ha_case
where
	hearing_id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);	


\echo '=== delete from ha_hearing'			
 delete
from
	ha_hearing
where
	id in (
	select
		distinct stream_id
	from
		tmp_selective_cu_stream);


-- Clear temporary table, once process completes
--   DROP TABLE IF EXISTS tmp_selective_cu_stream CASCADE;


EOF

rm -fr /tmp/streamids.csv
rm -fr /tmp/eventids_streamid.csv
