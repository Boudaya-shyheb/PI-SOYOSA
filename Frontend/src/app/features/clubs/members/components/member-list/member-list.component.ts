import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubMemberService } from '../../../../../services/club-member.service';
import { ClubJoinRequest, ClubMember } from '../../../../../models/member.model';
import { map, Observable } from 'rxjs';
import { AuthService } from '../../../../../services/auth.service';

type ClubMemberWithClubId = ClubMember & { clubId?: number | null };
type ClubMemberServiceWithClubFilter = ClubMemberService & {
  getByClubId?: (clubId: number) => Observable<ClubMember[]>;
};

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.css']
})
export class MemberListComponent implements OnInit {

  members: ClubMember[] = [];
  loading = false;
  errorMessage = '';
  clubId!: number;

  constructor(
    private memberService: ClubMemberService,
    private router: Router,
    private route: ActivatedRoute,
    public auth: AuthService
  ) { }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('clubId');
    this.clubId = idParam ? Number(idParam) : 0;
    this.loadMembers();
  }

  loadMembers(): void {
    this.loading = true;
    this.getMembersByClubId(this.clubId).subscribe({
      next: (data: ClubMember[]) => {
        this.members = data;
        this.loading = false;
      },
      error: (err: unknown) => {
        this.errorMessage = 'Error loading members';
        console.error(err);
        this.loading = false;
      }
    });
  }

  create(): void {
    this.router.navigate(['/clubs', this.clubId, 'members', 'create']);
  }

  edit(id?: number): void {
    if (!id) return;
    this.router.navigate(['/clubs', this.clubId, 'members', 'edit', id]);
  }

  delete(id?: number): void {
    if (!id) return;

    if (confirm('Are you sure you want to delete this member?')) {
      this.memberService.delete(id).subscribe({
        next: () => this.loadMembers(),
        error: (err: unknown) => console.error('Delete error', err)
      });
    }
  }

  private getMembersByClubId(clubId: number): Observable<ClubMember[]> {
    const serviceWithFilter = this.memberService as ClubMemberServiceWithClubFilter;
    if (serviceWithFilter.getByClubId) {
      return serviceWithFilter.getByClubId(clubId);
    }

    return this.memberService.getAll().pipe(
      map((members: ClubMember[]) =>
        (members as ClubMemberWithClubId[]).filter(member => member.clubId === clubId)
      )
    );
  }
}
