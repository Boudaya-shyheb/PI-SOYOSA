export interface ClubMember {
  id?: number;
  clubId?: number;
  firstName?: string;
  lastName?: string;
  email: string;
  phoneNumber?: string;
  age?: number;
  educationLevel?: string;
  role: 'MEMBER' | 'PRESIDENT' | 'VICE_PRESIDENT';
}

export type ClubJoinRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ClubJoinRequest {
  id?: number;
  clubId: number;
  email: string;
  firstName: string;
  lastName: string;
  status: ClubJoinRequestStatus;
  requestedAt: string;
  reviewedAt?: string | null;
}
