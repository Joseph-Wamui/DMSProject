export class FileTypeUtils {
  static getDocumentIcon(fileType: string): string {
     const mimeTypeToFileTypeMap: { [key: string]: string } = {
       'application/pdf': 'pdf',
       'application/msword': 'doc',
       'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
       'application/vnd.ms-excel': 'xls',
       'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
       'application/vnd.ms-powerpoint': 'ppt',
       'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx',
       'image/jpeg': 'jpg',
       'image/png': 'png',
       'image/gif': 'gif',
       // Add more MIME types as needed
     };
 
     const fileExtension = fileType.split('/').pop()?.toLowerCase();
 
     return mimeTypeToFileTypeMap[fileType] || 'description';
  }
 }