import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubMemberService } from '../../../../../services/club-member.service';
import { ClubService } from '../../../../../services/club.service';
import { ClubMember } from '../../../../../models/member.model';
import { SelectableUser, UserService } from '../../../../../services/user.service';
import { forkJoin } from 'rxjs';
import { map, Observable } from 'rxjs';

type ClubMemberPayload = ClubMember & { clubId: number };
type ClubMemberWithClubId = ClubMember & { clubId?: number | null };
type ClubMemberServiceWithClubFilter = ClubMemberService & {
  getByClubId?: (clubId: number) => Observable<ClubMember[]>;
};

@Component({
  selector: 'app-member-form',
  templateUrl: './member-form.component.html',
  styleUrls: ['./member-form.component.css']
})
export class MemberFormComponent implements OnInit {

  form!: FormGroup;
  clubId!: number;
  memberId?: number;
  isEditMode = false;
  isClubFull = false;
  isCapacityLoading = false;
  isSubmitting = false;
  isUsersLoading = false;
  errorMessage = '';
  selectableUsers: SelectableUser[] = [];

  roles = ['MEMBER', 'PRESIDENT', 'VICE_PRESIDENT'];

  constructor(
    private fb: FormBuilder,
    private memberService: ClubMemberService,
    private clubService: ClubService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Initialisation du formulaire réactif
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      role: ['', Validators.required]
    });

    this.loadSelectableUsers();

    // Récupération du clubId depuis l'URL (/clubs/:clubId/members/...)
    const clubParam = this.route.snapshot.paramMap.get('clubId');
    if (!clubParam) {
      this.errorMessage = 'Club not found.';
      this.form.disable({ emitEvent: false });
      return;
    }
    this.clubId = +clubParam;

    // Mode édition si :id est présent
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.memberId = +id;
      this.isEditMode = true;

      this.memberService.getById(this.memberId).subscribe(member => {
        this.form.patchValue(member);
      });
      return;
    }

    // Validation métier capacité maxMembers uniquement en création
    this.checkClubCapacity();
  }

  submit(): void {
    if (this.form.invalid || this.isCapacityLoading || this.isClubFull || this.isSubmitting) {
      this.form.markAllAsTouched();
      return;
    }

    const data: ClubMemberPayload = {
      ...this.form.getRawValue(),
      clubId: this.clubId
    };

    this.isSubmitting = true;
    this.errorMessage = '';

    if (this.isEditMode && this.memberId) {
      this.memberService.update(this.memberId, data)
        .subscribe({
          next: () => {
            this.isSubmitting = false;
            this.router.navigate(['/clubs', this.clubId, 'members']);
          },
          error: (err) => {
            this.isSubmitting = false;
            this.errorMessage = err?.error?.message ?? 'Unable to save member. Please try again.';
          }
        });
    } else {
      this.memberService.create(data)
        .subscribe({
          next: () => {
            this.isSubmitting = false;
            this.router.navigate(['/clubs', this.clubId, 'members']);
          },
          error: (err) => {
            this.isSubmitting = false;
            this.errorMessage = err?.error?.message ?? 'Unable to create member. Please try again.';
          }
        });
    }
  }

  cancel(): void {
    this.router.navigate(['/clubs', this.clubId, 'members']);
  }

  // Vérifie maxMembers côté frontend avant autorisation de création
  private checkClubCapacity(): void {
    this.isCapacityLoading = true;
    this.errorMessage = '';
    this.form.disable({ emitEvent: false });

    forkJoin({
      club: this.clubService.getById(this.clubId),
      members: this.getMembersByClubId(this.clubId)
    }).subscribe({
      next: ({ club, members }) => {
        const membersForClub = (members as ClubMemberWithClubId[]).filter(
          member => member.clubId === this.clubId
        );
        const maxMembers = club.maxMembers ?? Number.POSITIVE_INFINITY;

        this.isClubFull = membersForClub.length >= maxMembers;
        this.isCapacityLoading = false;

        if (this.isClubFull) {
          this.form.disable({ emitEvent: false });
          return;
        }

        this.form.enable({ emitEvent: false });
      },
      error: () => {
        this.isCapacityLoading = false;
        this.errorMessage = 'Unable to validate club capacity. Please refresh and try again.';
        this.form.disable({ emitEvent: false });
      }
    });
  }

  private loadSelectableUsers(): void {
    this.isUsersLoading = true;
    this.userService.getSelectableUsers().subscribe({
      next: (users) => {
        this.selectableUsers = users;
        this.isUsersLoading = false;
      },
      error: () => {
        this.isUsersLoading = false;
        this.errorMessage = 'Impossible de charger la liste des utilisateurs.';
      }
    });
  }

  // Utilise getByClubId si disponible, avec fallback sécurisé sans casser l'existant
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
