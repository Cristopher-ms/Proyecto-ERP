export type Role = 'ROLE_ADMIN' | 'ROLE_SELLER' | 'ROLE_WAREHOUSE';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  roleDisplayName: string;
  active: boolean;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  user: User;
}
