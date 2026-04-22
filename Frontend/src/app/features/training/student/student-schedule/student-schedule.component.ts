import { Component, OnInit } from '@angular/core';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { TrainingEnrollmentService, Enrollment } from '../../../../services/training-enrollment.service';

@Component({
    selector: 'app-student-schedule',
    templateUrl: './student-schedule.component.html',
    styleUrls: ['./student-schedule.component.css']
})
export class StudentScheduleComponent implements OnInit {
    calendarOptions: CalendarOptions = {
        initialView: 'timeGridWeek',
        plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        events: [],
        slotMinTime: '00:00:00',
        slotMaxTime: '24:00:00',
        allDaySlot: true,
        height: 'auto',
        themeSystem: 'standard',
        nowIndicator: true
    };

    constructor(private enrollmentService: TrainingEnrollmentService) { }

    ngOnInit(): void {
        this.loadEvents();
    }

    loadEvents(): void {
        this.enrollmentService.getMyEnrollments(0, 1000).subscribe((response: any) => {
            const enrollments: Enrollment[] = response.content;
            const events = enrollments
                .filter((e: Enrollment) => e.session != null)
                .map((e: Enrollment) => {
                    const start = this.combineDateAndTime(e.session.date, e.session.startTime);
                    const end = new Date(start.getTime() + e.session.duration * 60000);

                    return {
                        title: `${e.training.title} (${e.training.type === 'ONLINE' ? 'Online' : 'In-Person'})`,
                        start: start,
                        end: end,
                        backgroundColor: e.training.type === 'ONLINE' ? '#4f46e5' : '#10b981', // Indigo for online, Emerald for offline
                        borderColor: 'transparent',
                        extendedProps: {
                            enrollment: e
                        }
                    };
                });

            this.calendarOptions = { ...this.calendarOptions, events: events };
        });
    }

    private combineDateAndTime(dateStr: string | Date, timeStr: string): Date {
        const d = new Date(dateStr);
        // Create date in local time using year, month, day components to avoid UTC shifts
        const year = d.getFullYear();
        const month = d.getMonth();
        const day = d.getDate();

        const date = new Date(year, month, day);

        if (timeStr) {
            const parts = timeStr.split(':');
            date.setHours(parseInt(parts[0], 10) || 0);
            date.setMinutes(parseInt(parts[1], 10) || 0);
            date.setSeconds(parseInt(parts[2], 10) || 0);
        }

        return date;
    }
}
