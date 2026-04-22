import { Injectable } from '@angular/core';
import { BehaviorSubject, interval, Subscription } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class TimerService {

    private timeLeftSubject = new BehaviorSubject<number>(0);
    timeLeft$ = this.timeLeftSubject.asObservable();

    private timerSub?: Subscription;

    start(seconds: number) {
        this.stop();

        this.timeLeftSubject.next(seconds);

        this.timerSub = interval(1000).subscribe(() => {
            const current = this.timeLeftSubject.value - 1;
            this.timeLeftSubject.next(current);

            if (current <= 0) {
                this.stop();
            }
        });
    }

    stop() {
        if (this.timerSub) {
            this.timerSub.unsubscribe();
        }
    }

    reset(seconds: number) {
        this.start(seconds);
    }

    getTime() {
        return this.timeLeftSubject.value;
    }
}