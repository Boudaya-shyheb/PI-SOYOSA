import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, map, Observable } from 'rxjs';
import { Club } from '../../../../../models/club.model';
import { ClubMember } from '../../../../../models/member.model';
import { ClubService } from '../../../../../services/club.service';
import { ClubMemberService } from '../../../../../services/club-member.service';

type ClubMemberWithClubId = ClubMember & { clubId?: number | null };
type ClubMemberServiceWithClubFilter = ClubMemberService & {
  getByClubId?: (clubId: number) => Observable<ClubMember[]>;
};

@Component({
  selector: 'app-member-stats',
  templateUrl: './member-stats.component.html',
  styleUrls: ['./member-stats.component.css']
})
export class MemberStatsComponent implements OnInit {
  clubId!: number;
  club: Club | null = null;
  members: ClubMember[] = [];
  loading = false;
  errorMessage = '';
  fillPercent = 0;
  averageAge = 0;
  roleStats: Array<{ role: string; count: number }> = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private clubService: ClubService,
    private memberService: ClubMemberService
  ) {}

  ngOnInit(): void {
    const clubParam = this.route.snapshot.paramMap.get('clubId');
    if (!clubParam) {
      this.errorMessage = 'Club not found.';
      return;
    }

    this.clubId = +clubParam;
    this.loadStats();
  }

  goToMembersList(): void {
    this.router.navigate(['/clubs', this.clubId, 'members']);
  }

  private loadStats(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      club: this.clubService.getById(this.clubId),
      members: this.getMembersByClubId(this.clubId)
    }).subscribe({
      next: ({ club, members }) => {
        this.club = club;
        this.members = members;
        this.computeStats();
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load members statistics.';
        this.loading = false;
      }
    });
  }

  private computeStats(): void {
    const maxMembers = this.club?.maxMembers ?? 0;
    this.fillPercent = maxMembers > 0
      ? Math.min(100, Math.round((this.members.length / maxMembers) * 100))
      : 0;

    const ages = this.members
      .map(member => member.age)
      .filter((age): age is number => age !== undefined && age !== null && age > 0);
    this.averageAge = ages.length > 0
      ? Math.round((ages.reduce((sum, age) => sum + age, 0) / ages.length) * 10) / 10
      : 0;

    const counts = new Map<string, number>();
    this.members.forEach((member) => {
      const currentCount = counts.get(member.role) ?? 0;
      counts.set(member.role, currentCount + 1);
    });
    this.roleStats = Array.from(counts.entries()).map(([role, count]) => ({ role, count }));
  }

  getMemberCompletion(member: ClubMember): number {
    const fields = [
      member.firstName,
      member.lastName,
      member.email,
      member.phoneNumber,
      member.age,
      member.educationLevel,
      member.role
    ];
    const completed = fields.filter(value => value !== undefined && value !== null && value !== '').length;
    return Math.round((completed / fields.length) * 100);
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
