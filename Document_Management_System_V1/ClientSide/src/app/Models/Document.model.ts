export interface Document{
    id?: number;
    documentName?: string;
    documentData?: string;
    fileSize?: number;
    filepath?: string;
    createdBy?: string;
    createDate?: Date;
    dueDate?: Date;
    tags?: string[];
    notes?: string;
    department?: string;
    fileType?: string;
    approverComments?: string;
    versionNumber?: string;
}