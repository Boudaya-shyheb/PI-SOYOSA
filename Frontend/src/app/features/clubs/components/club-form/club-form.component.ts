import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubService } from '../../../../services/club.service';
import { Club } from '../../../../models/club.model';

@Component({
  selector: 'app-club-form',
  templateUrl: './club-form.component.html'
})
export class ClubFormComponent implements OnInit {

  clubForm!: FormGroup;
  clubId: number | null = null;
  isEditMode = false;
  occupiedClasses: string[] = [];

  constructor(
    private fb: FormBuilder,
    private clubService: ClubService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    // Initialisation du formulaire
    this.clubForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      meetingLocation: [''],
      meetingSchedule: [''],
      maxMembers: [1, [Validators.required, Validators.min(1)]]
    });

    // Vérifier si on est en mode EDIT
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.clubId = +idParam;
      this.isEditMode = true;
      this.loadClub(this.clubId);
    }
    this.loadOccupiedClasses();
  }

  loadOccupiedClasses() {
    this.clubService.getAll().subscribe({
      next: (clubs) => {
        const otherClubs = this.isEditMode ? clubs.filter(c => c.id !== this.clubId) : clubs;
        this.occupiedClasses = otherClubs.map(c => c.meetingLocation || '').filter(loc => !!loc);
      },
      error: (err) => console.error('Failed to load clubs', err)
    });
  }

  isOccupied(room: string): boolean {
    return this.occupiedClasses.includes(room);
  }

  selectClass(room: string) {
    if (!this.isOccupied(room)) {
      this.clubForm.patchValue({ meetingLocation: room });
    }
  }

  getRoomStyle(room: string) {
    const isEditingThisRoom = this.clubForm.get('meetingLocation')?.value === room;
    const occupied = this.isOccupied(room);

    let base = 'relative flex flex-col items-center justify-center font-bold transition-all duration-500 border-2 ';
    
    if (occupied) {
      base += 'bg-gray-800/90 text-gray-500 border-gray-700 cursor-not-allowed [transform:translateZ(2px)] ';
    } else if (isEditingThisRoom) {
      base += 'bg-indigo-500/90 text-white border-indigo-300 cursor-pointer z-20 [transform:translateZ(60px)] shadow-[0_0_30px_rgba(99,102,241,0.8)] ring-2 ring-indigo-300/50 ';
    } else {
      base += 'bg-white/10 text-gray-200 border-white/20 cursor-pointer hover:bg-white/20 z-10 [transform:translateZ(15px)] hover:[transform:translateZ(35px)] hover:shadow-[0_0_25px_rgba(255,255,255,0.3)] hover:border-white/50 ';
    }
    return base;
  }

  // Charger club pour édition
  loadClub(id: number): void {
    this.clubService.getById(id).subscribe({
      next: (club) => this.clubForm.patchValue(club),
      error: (err) => console.error('Error loading club', err)
    });
  }

  // Submit
  onSubmit(): void {
    if (this.clubForm.invalid) {
      this.clubForm.markAllAsTouched();
      return;
    }

    const clubData: Club = this.clubForm.value;

    if (this.isEditMode && this.clubId) {
      this.clubService.update(this.clubId, clubData).subscribe({
        next: () => this.router.navigate(['/clubs']),
        error: (err) => console.error('Update error', err)
      });
    } else {
      this.clubService.create(clubData).subscribe({
        next: () => this.router.navigate(['/clubs']),
        error: (err) => console.error('Create error', err)
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/clubs']);
  }
}
