import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class CustomValidators {
    /**
     * Validates that the selected date is in the future.
     * If the date is today, it's considered valid.
     */
    static futureDate(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (!control.value) return null;

            const inputDate = new Date(control.value);
            const today = new Date();

            // Reset hours for pure date comparison
            today.setHours(0, 0, 0, 0);
            inputDate.setHours(0, 0, 0, 0);

            return inputDate >= today ? null : { pastDate: true };
        };
    }

    /**
     * Validates that a string is a valid URL.
     */
    static url(): ValidatorFn {
        const urlPattern = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([\/\w .-]*)*\/?$/;
        return (control: AbstractControl): ValidationErrors | null => {
            if (!control.value) return null;
            return urlPattern.test(control.value) ? null : { invalidUrl: true };
        };
    }
}
