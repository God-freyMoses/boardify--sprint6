export type UserRoleType = 'USER' | 'HR_MANAGER' | 'NEW_HIRE';

export type LoginRequestType = {
  email: string;
  password: string;
};

export type RegisterRequestType = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  companyName: string;
  role: UserRoleType;
};

export type UserType = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRoleType;
  createdAt: string;
  userType?: string;
}

export type UserTokenType = {
  token: string;
  user: UserType;
};


export type ResultType = {
  success: boolean;
  message: string;
  statusCode: number;
  data: UserTokenType;
}


export type AuthStateType = {
  user: UserType | null;
  token: string | null;
  loading: boolean;
  isLoggedIn: boolean;
  error: string | null;
}
