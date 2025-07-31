export interface NewHireType {
  firstName: string;
  lastName: string;
  email: string;
  departmentId: string; // Now represents department name
  role: string;
  gender: string;
  title: string;
  pictureUrl: string;
  password: string;
}

export type ResultType <T = any> = {
  success: boolean;
  message: string;
  statusCode: number;
  data: T;
}

