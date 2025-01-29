// src/app/models/search-criteria.model.ts 

export interface SearchCriteria {
    documentName?: string;
    createdBy?: string;
    startDate?: string;
    endDate?: string;
    notes?: string;
    department?: string;
    fileType?: string;
    approverComments?: string;
    status?: string;
   }
   