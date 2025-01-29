package com.emt.dms1.user;



import lombok.*;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum Permission{
    Upload_documents("Upload_documents"),
    View_documents("View_documents"),
    Read_metadata("Read_metadata"),
    Approve_documents("Approve_documents"),
    Reject_documents("Reject_documents"),
    View_audit_logs("View_audit_logs"),
    Generate_reports("Generate_reports"),
    Set_access_controls("Set_access_controls"),
    Create_users("Create_users"),
    Update_users("Update_users"),
    Update_attributes("Update_attributes"),
    Edit_documents("Edit_documents"),
    Delete_documents("Delete_documents"),
    Download_documents("Download_documents"),
    Share_documents("Share_documents"),
    Backup_documents("Backup_documents"),
    Archive_documents("Archive_documents"),
    Restore_documents("Restore_documents"),
    Get_user_by_id("Get_user_by_id"),
    Search_documents("Search_documents"),
    Fetch_all_users("Fetch_all_users"),
    Get_documents_by_id("Get_documents_by_id"),
    Fetch_document_names_list("Fetch_document_names_list"),
    Fetch_all_documents("Fetch_all_documents"),
    Delete_document_by_id("Delete_document_by_id"),
    Create_role("Create_role"),
    Get_user_by_employee_number("Get_user_by_employee_number"),
    Delete_document_version("Delete_document_version"),
    Get_all_versions_of_a_document("Get_all_versions_of_a_document"),
    Add_a_document_version("Add_a_document_version"),
    Get_a_version_of_a_document("Get_a_version_of_a_document"),
    Delete_version_of_a_document("Delete_version_of_a_document"),
    Update_document_attributes("Update_document_attributes"),
    Upload_multiple_documents("Upload_multiple_documents"),
    Get_document_status("Get_document_status"),
    Get_document_by_id("Get_document_by_id"),
    Get_Archived_Documents("Get_Archived_Documents"),
    Set_retention_period("Set_retention_period"),
    Schedule_Archiving("schedule_Archiving"),
    Schedule_Backup("schedule_Backup"),
    Delete_users("Delete_users");

    @Getter
    private final String permission;


}