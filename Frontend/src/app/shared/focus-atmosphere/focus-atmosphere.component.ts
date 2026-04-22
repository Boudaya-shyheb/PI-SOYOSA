import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { AtmosphereTrack, FocusAtmosphereService } from '../../services/focus-atmosphere.service';

@Component({
  selector: 'app-focus-atmosphere',
  templateUrl: './focus-atmosphere.component.html',
  styleUrls: ['./focus-atmosphere.component.css']
})
export class FocusAtmosphereComponent implements OnInit, OnDestroy {
  isVisible = false;
  panelOpen = false;

  activeTrackId: string | null = null;
  isPlaying = false;
  volumePercent = 60;
  errorMessage = '';

  readonly tracks: AtmosphereTrack[];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly focusAtmosphere: FocusAtmosphereService
  ) {
    this.tracks = this.focusAtmosphere.tracks;
  }

  ngOnInit(): void {
    this.bindState();
    this.syncVisibilityWithRoute(this.router.url);

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event) => {
      this.syncVisibilityWithRoute((event as NavigationEnd).urlAfterRedirects);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  togglePanel(): void {
    this.panelOpen = !this.panelOpen;
  }

  playOrPause(trackId: string): void {
    if (this.activeTrackId !== trackId) {
      this.focusAtmosphere.playTrack(trackId);
      return;
    }

    this.focusAtmosphere.togglePlayback();
  }

  updateVolume(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.focusAtmosphere.setVolume(Number(target.value));
  }

  isTrackPlaying(trackId: string): boolean {
    return this.activeTrackId === trackId && this.isPlaying;
  }

  private bindState(): void {
    this.focusAtmosphere.activeTrackId$.pipe(takeUntil(this.destroy$)).subscribe(trackId => {
      this.activeTrackId = trackId;
    });

    this.focusAtmosphere.isPlaying$.pipe(takeUntil(this.destroy$)).subscribe(playing => {
      this.isPlaying = playing;
    });

    this.focusAtmosphere.volume$.pipe(takeUntil(this.destroy$)).subscribe(volume => {
      this.volumePercent = Math.round(volume * 100);
    });

    this.focusAtmosphere.errorMessage$.pipe(takeUntil(this.destroy$)).subscribe(message => {
      this.errorMessage = message;
    });
  }

  private syncVisibilityWithRoute(url: string): void {
    const path = url.split('?')[0];
    const inCourseScope = this.isCourseRoute(path);

    if (!inCourseScope && this.isVisible) {
      this.panelOpen = false;
      this.focusAtmosphere.stopAndReset();
    }

    this.isVisible = inCourseScope;
  }

  private isCourseRoute(path: string): boolean {
    return /^\/courses(\/|$)/.test(path)
      || /^\/course-view(\/|$)/.test(path)
      || /^\/lessons(\/|$)/.test(path)
      || /^\/training-details(\/|$)/.test(path);
  }
}
