# --- !Ups

DELETE FROM ACTIVITY_TYPE WHERE NAME IN (
  'AttendanceMonitoringUnrecordedPoints',  'AttendanceMonitoringUnrecordedStudents',
  'BulkNewAgentRelationship',  'BulkOldAgentRelationship',
  'BulkStudentRelationship',  'CancelledBulkStudentRelationshipChangeToAgent',
  'CancelledStudentRelationshipChangeToNewAgent',  'CancelledStudentRelationshipChangeToOldAgent',
  'CancelledStudentRelationshipChangeToStudent',  'Cm2MarkedPlagarised',
  'Cm2RequestAssignmentAccess',  'editedMeetingRecordApproval',
  'ExamMarked',  'ExamReleased',
  'ExtensionChanged',  'ExtensionGranted',
  'ExtensionMoreInfoReceived',  'ExtensionRequestApproved',
  'ExtensionRequestCreated',  'ExtensionRequestModified',
  'ExtensionRequestMoreInfo',  'ExtensionRequestRejected',
  'ExtensionRequestRespondedApprove',  'ExtensionRequestRespondedMoreInfo',
  'ExtensionRequestRespondedReject',  'ExtensionRevoked',
  'FeedbackAdjustment',  'FeedbackChange',
  'FeedbackDueExtension',  'FeedbackDueGeneral',
  'FeedbackPublished',  'FinaliseFeedback',
  'MarkedPlagarised',  'meetingRecordApproved',
  'meetingRecordRejected',  'MissedAttendanceMonitoringCheckpointsHigh',
  'MissedAttendanceMonitoringCheckpointsLow',  'MissedAttendanceMonitoringCheckpointsMedium',
  'ModeratorRejected',  'newMeetingRecordApproval',
  'OpenSmallGroupSets',  'OpenSmallGroupSetsOtherSignUp',
  'ReleaseSmallGroupSets',  'ReleaseToMarker',
  'RequestAssignmentAccess',  'ReturnToMarker',
  'ScheduledMeetingRecordBehalf',  'ScheduledMeetingRecordConfirm',
  'ScheduledMeetingRecordInvitee',  'ScheduledMeetingRecordMissedInvitee',
  'ScheduledMeetingRecordReminderAgent',  'ScheduledMeetingRecordReminderStudent',
  'SmallGroupEventAttendanceReminder',  'SmallGroupSetChangedStudent',
  'SmallGroupSetChangedTutor',  'StudentFeedbackAdjustment',
  'StudentRelationshipChangeToNewAgent',  'StudentRelationshipChangeToOldAgent',
  'StudentRelationshipChangeToStudent',  'SubmissionDueExtension',
  'SubmissionDueGeneral',  'SubmissionReceipt',
  'SubmissionReceived',  'TurnitinJobError',
  'TurnitinJobSuccess',  'UnlinkedAttendanceMonitoringScheme',
  'UnlinkedDepartmentSmallGroupSet'
);

INSERT INTO ACTIVITY_TYPE VALUES ('AttendanceMonitoringUnrecordedPoints','Unrecorded monitoring points');
INSERT INTO ACTIVITY_TYPE VALUES ('AttendanceMonitoringUnrecordedStudents','Students have unrecorded monitoring points');
INSERT INTO ACTIVITY_TYPE VALUES ('BulkNewAgentRelationship','New student relationships added');
INSERT INTO ACTIVITY_TYPE VALUES ('BulkOldAgentRelationship','Student relationships ended');
INSERT INTO ACTIVITY_TYPE VALUES ('BulkStudentRelationship','Relationship changes');
INSERT INTO ACTIVITY_TYPE VALUES ('CancelledBulkStudentRelationshipChangeToAgent','Scheduled relationship changes cancelled');
INSERT INTO ACTIVITY_TYPE VALUES ('CancelledStudentRelationshipChangeToNewAgent','Scheduled new student relationships cancelled');
INSERT INTO ACTIVITY_TYPE VALUES ('CancelledStudentRelationshipChangeToOldAgent','Scheduled end of student relationships cancelled');
INSERT INTO ACTIVITY_TYPE VALUES ('CancelledStudentRelationshipChangeToStudent','Scheduled relationship change cancelled');
INSERT INTO ACTIVITY_TYPE VALUES ('Cm2MarkedPlagarised','Submission marked plagiarised');
INSERT INTO ACTIVITY_TYPE VALUES ('Cm2RequestAssignmentAccess','Request assignment access');
INSERT INTO ACTIVITY_TYPE VALUES ('editedMeetingRecordApproval','Edited meeting record requires approval');
INSERT INTO ACTIVITY_TYPE VALUES ('ExamMarked','Exam marks added');
INSERT INTO ACTIVITY_TYPE VALUES ('ExamReleased','Exam released for marking');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionChanged','Extension deadline changed');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionGranted','Extension granted');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionMoreInfoReceived','More extension information received');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestApproved','Extension request approved');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestCreated','Extension requested');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestModified','Extension request modified');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestMoreInfo','Extension request returned for more information');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestRejected','Extension request rejected');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestRespondedApprove','Extension request approved');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestRespondedMoreInfo','Extension request returned for more information');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRequestRespondedReject','Extension request rejected');
INSERT INTO ACTIVITY_TYPE VALUES ('ExtensionRevoked','Extension revoked');
INSERT INTO ACTIVITY_TYPE VALUES ('FeedbackAdjustment','Feedback adjusted');
INSERT INTO ACTIVITY_TYPE VALUES ('FeedbackChange','Feedback changed');
INSERT INTO ACTIVITY_TYPE VALUES ('FeedbackDueExtension','Feedback due for extension');
INSERT INTO ACTIVITY_TYPE VALUES ('FeedbackDueGeneral','Feedback due');
INSERT INTO ACTIVITY_TYPE VALUES ('FeedbackPublished','Feedback published');
INSERT INTO ACTIVITY_TYPE VALUES ('FinaliseFeedback','Feedback marked and sent to admin');
INSERT INTO ACTIVITY_TYPE VALUES ('MarkedPlagarised','Submission marked plagiarised');
INSERT INTO ACTIVITY_TYPE VALUES ('meetingRecordApproved','Meeting record approved');
INSERT INTO ACTIVITY_TYPE VALUES ('meetingRecordRejected','Meeting record rejected');
INSERT INTO ACTIVITY_TYPE VALUES ('MissedAttendanceMonitoringCheckpointsHigh','Missed monitoring points (high)');
INSERT INTO ACTIVITY_TYPE VALUES ('MissedAttendanceMonitoringCheckpointsLow','Missed monitoring points (low)');
INSERT INTO ACTIVITY_TYPE VALUES ('MissedAttendanceMonitoringCheckpointsMedium','Missed monitoring points (medium)');
INSERT INTO ACTIVITY_TYPE VALUES ('ModeratorRejected','Feedback rejected by moderator');
INSERT INTO ACTIVITY_TYPE VALUES ('newMeetingRecordApproval','New meeting record requires approval');
INSERT INTO ACTIVITY_TYPE VALUES ('OpenSmallGroupSets','Small group sets open');
INSERT INTO ACTIVITY_TYPE VALUES ('OpenSmallGroupSetsOtherSignUp','Small group sets open');
INSERT INTO ACTIVITY_TYPE VALUES ('ReleaseSmallGroupSets','Small group sets released');
INSERT INTO ACTIVITY_TYPE VALUES ('ReleaseToMarker','Submissions released for marking');
INSERT INTO ACTIVITY_TYPE VALUES ('RequestAssignmentAccess','Request assignment access');
INSERT INTO ACTIVITY_TYPE VALUES ('ReturnToMarker','Submission returned to marker');
INSERT INTO ACTIVITY_TYPE VALUES ('ScheduledMeetingRecordBehalf','Scheduled meeting record created on your behalf');
INSERT INTO ACTIVITY_TYPE VALUES ('ScheduledMeetingRecordConfirm','Scheduled meeting record confirmed');
INSERT INTO ACTIVITY_TYPE VALUES ('ScheduledMeetingRecordInvitee','Scheduled meeting record created');
INSERT INTO ACTIVITY_TYPE VALUES ('ScheduledMeetingRecordMissedInvitee','Scheduled meeting record missed');
INSERT INTO ACTIVITY_TYPE VALUES ('ScheduledMeetingRecordReminderAgent','Scheduled meeting record reminder');
INSERT INTO ACTIVITY_TYPE VALUES ('ScheduledMeetingRecordReminderStudent','Scheduled meeting record reminder');
INSERT INTO ACTIVITY_TYPE VALUES ('SmallGroupEventAttendanceReminder','Small group event attendance needs recording');
INSERT INTO ACTIVITY_TYPE VALUES ('SmallGroupSetChangedStudent','Small group set changed');
INSERT INTO ACTIVITY_TYPE VALUES ('SmallGroupSetChangedTutor','Small group set changed');
INSERT INTO ACTIVITY_TYPE VALUES ('StudentFeedbackAdjustment','Feedback adjusted');
INSERT INTO ACTIVITY_TYPE VALUES ('StudentRelationshipChangeToNewAgent','New student relationship added');
INSERT INTO ACTIVITY_TYPE VALUES ('StudentRelationshipChangeToOldAgent','Student relationship ended');
INSERT INTO ACTIVITY_TYPE VALUES ('StudentRelationshipChangeToStudent','Relationship changed');
INSERT INTO ACTIVITY_TYPE VALUES ('SubmissionDueExtension','Submission due for extension');
INSERT INTO ACTIVITY_TYPE VALUES ('SubmissionDueGeneral','Submission due');
INSERT INTO ACTIVITY_TYPE VALUES ('SubmissionReceipt','Submission receipt');
INSERT INTO ACTIVITY_TYPE VALUES ('SubmissionReceived','Submission received');
INSERT INTO ACTIVITY_TYPE VALUES ('TurnitinJobError','Turnitin job error');
INSERT INTO ACTIVITY_TYPE VALUES ('TurnitinJobSuccess','Turnitin job success');
INSERT INTO ACTIVITY_TYPE VALUES ('UnlinkedAttendanceMonitoringScheme','Monitoring point scheme unlinked from SITS');
INSERT INTO ACTIVITY_TYPE VALUES ('UnlinkedDepartmentSmallGroupSet','Reusable small group set unlinked from SITS');

# --- !Downs

DELETE FROM ACTIVITY_TYPE WHERE NAME IN (
  'AttendanceMonitoringUnrecordedPoints',  'AttendanceMonitoringUnrecordedStudents',
  'BulkNewAgentRelationship',  'BulkOldAgentRelationship',
  'BulkStudentRelationship',  'CancelledBulkStudentRelationshipChangeToAgent',
  'CancelledStudentRelationshipChangeToNewAgent',  'CancelledStudentRelationshipChangeToOldAgent',
  'CancelledStudentRelationshipChangeToStudent',  'Cm2MarkedPlagarised',
  'Cm2RequestAssignmentAccess',  'editedMeetingRecordApproval',
  'ExamMarked',  'ExamReleased',
  'ExtensionChanged',  'ExtensionGranted',
  'ExtensionMoreInfoReceived',  'ExtensionRequestApproved',
  'ExtensionRequestCreated',  'ExtensionRequestModified',
  'ExtensionRequestMoreInfo',  'ExtensionRequestRejected',
  'ExtensionRequestRespondedApprove',  'ExtensionRequestRespondedMoreInfo',
  'ExtensionRequestRespondedReject',  'ExtensionRevoked',
  'FeedbackAdjustment',  'FeedbackChange',
  'FeedbackDueExtension',  'FeedbackDueGeneral',
  'FeedbackPublished',  'FinaliseFeedback',
  'MarkedPlagarised',  'meetingRecordApproved',
  'meetingRecordRejected',  'MissedAttendanceMonitoringCheckpointsHigh',
  'MissedAttendanceMonitoringCheckpointsLow',  'MissedAttendanceMonitoringCheckpointsMedium',
  'ModeratorRejected',  'newMeetingRecordApproval',
  'OpenSmallGroupSets',  'OpenSmallGroupSetsOtherSignUp',
  'ReleaseSmallGroupSets',  'ReleaseToMarker',
  'RequestAssignmentAccess',  'ReturnToMarker',
  'ScheduledMeetingRecordBehalf',  'ScheduledMeetingRecordConfirm',
  'ScheduledMeetingRecordInvitee',  'ScheduledMeetingRecordMissedInvitee',
  'ScheduledMeetingRecordReminderAgent',  'ScheduledMeetingRecordReminderStudent',
  'SmallGroupEventAttendanceReminder',  'SmallGroupSetChangedStudent',
  'SmallGroupSetChangedTutor',  'StudentFeedbackAdjustment',
  'StudentRelationshipChangeToNewAgent',  'StudentRelationshipChangeToOldAgent',
  'StudentRelationshipChangeToStudent',  'SubmissionDueExtension',
  'SubmissionDueGeneral',  'SubmissionReceipt',
  'SubmissionReceived',  'TurnitinJobError',
  'TurnitinJobSuccess',  'UnlinkedAttendanceMonitoringScheme',
  'UnlinkedDepartmentSmallGroupSet'
);

