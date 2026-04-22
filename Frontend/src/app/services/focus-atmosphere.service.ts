import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface AtmosphereTrack {
  id: string;
  name: string;
  icon: string;
  src: string;
}

@Injectable({ providedIn: 'root' })
export class FocusAtmosphereService {
  readonly tracks: AtmosphereTrack[] = [
    { id: 'rain', name: 'Rain', icon: '🌧️', src: 'https://actions.google.com/sounds/v1/weather/rain_on_roof.ogg' },
    { id: 'cafe', name: 'Cafe', icon: '☕', src: 'https://actions.google.com/sounds/v1/ambiences/coffee_shop.ogg' },
    { id: 'lofi', name: 'Lo-fi', icon: '🎧', src: 'https://actions.google.com/sounds/v1/science_fiction/windchime_drone.ogg' },
    { id: 'ocean', name: 'Ocean', icon: '🌊', src: 'https://actions.google.com/sounds/v1/water/waves_crashing_on_rock_beach.ogg' },
    { id: 'fireplace', name: 'Fireplace', icon: '🔥', src: 'https://actions.google.com/sounds/v1/ambiences/fire.ogg' }
  ];

  private readonly defaultVolume = 0.6;
  private audio: HTMLAudioElement | null = null;

  readonly activeTrackId$ = new BehaviorSubject<string | null>(null);
  readonly isPlaying$ = new BehaviorSubject<boolean>(false);
  readonly volume$ = new BehaviorSubject<number>(this.defaultVolume);
  readonly errorMessage$ = new BehaviorSubject<string>('');

  playTrack(trackId: string): void {
    const track = this.tracks.find(item => item.id === trackId);
    if (!track) {
      return;
    }

    this.errorMessage$.next('');

    if (this.activeTrackId$.value !== track.id) {
      this.disposeAudio();
      this.audio = new Audio(track.src);
      this.audio.loop = true;
      this.audio.volume = this.volume$.value;
      this.audio.addEventListener('ended', () => {
        this.isPlaying$.next(false);
      });
      this.activeTrackId$.next(track.id);
    }

    this.audio?.play().then(() => {
      this.isPlaying$.next(true);
    }).catch(() => {
      this.isPlaying$.next(false);
      this.errorMessage$.next('Unable to stream this sound right now. Please check your internet connection and try again.');
    });
  }

  togglePlayback(trackId?: string): void {
    if (!this.activeTrackId$.value && trackId) {
      this.playTrack(trackId);
      return;
    }

    if (!this.audio) {
      return;
    }

    if (this.isPlaying$.value) {
      this.audio.pause();
      this.isPlaying$.next(false);
    } else {
      this.audio.play().then(() => {
        this.isPlaying$.next(true);
      }).catch(() => {
        this.isPlaying$.next(false);
        this.errorMessage$.next('Unable to resume playback.');
      });
    }
  }

  setVolume(percent: number): void {
    const clamped = Math.max(0, Math.min(100, percent));
    const normalized = clamped / 100;
    this.volume$.next(normalized);

    if (this.audio) {
      this.audio.volume = normalized;
    }
  }

  stopAndReset(): void {
    this.disposeAudio();
    this.activeTrackId$.next(null);
    this.isPlaying$.next(false);
    this.volume$.next(this.defaultVolume);
    this.errorMessage$.next('');
  }

  getActiveTrack(): AtmosphereTrack | null {
    return this.tracks.find(track => track.id === this.activeTrackId$.value) ?? null;
  }

  private disposeAudio(): void {
    if (!this.audio) {
      return;
    }

    this.audio.pause();
    this.audio.currentTime = 0;
    this.audio.src = '';
    this.audio.load();
    this.audio = null;
  }
}
