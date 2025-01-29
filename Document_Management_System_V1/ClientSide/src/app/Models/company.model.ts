export interface Company {
    organizationId: number;
    organizationName: string;
    postalAddress: string;
    phoneNumber: string;
    officeLocation: string;
    country: string;
    emailAddress: string;
    website_URL: string;
    logo: File | null;
    logoName: string;
    logData: string;
    createData: Date;
  }
  