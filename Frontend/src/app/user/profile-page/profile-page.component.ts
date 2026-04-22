import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth/auth.service';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';
import {ProfileService} from "../../services/auth/profile.service";

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-page.component.html',
  styleUrls: []
})
export class ProfilePageComponent implements OnInit {
  userProfile: any = {
    firstName: '',
    lastName: '',
    mail: '',
    phoneNumber: '',
    address: ''
  };
  originalProfile: any = {};

  passwordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  isLoading = true;
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  constructor(private profileService: ProfileService, private router: Router, private auth: AuthService) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.profileService.getProfile().subscribe({
      next: (profile: any) => {
        this.userProfile = profile;
        this.originalProfile = JSON.parse(JSON.stringify(profile));
        this.isLoading = false;
        if (this.userProfile.image) {
          this.previewUrl = this.userProfile.image;
        }
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        Swal.fire('Error', 'Could not load profile data.', 'error');
        this.isLoading = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
      this.uploadImage();
    }
  }

  uploadImage(): void {
    if (!this.selectedFile) return;

    const email = this.auth.getUserEmail();
    if (!email) {
      Swal.fire('Error', 'User email not found.', 'error');
      return;
    }

    Swal.fire({
      title: 'Uploading...',
      text: 'Please wait while we upload your profile picture.',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    this.profileService.uploadProfileImage(email, this.selectedFile).subscribe({
      next: (response: any) => {
        Swal.fire('Success', 'Profile picture updated successfully!', 'success');
        this.userProfile.image = response.image;
        this.previewUrl = response.image;
        this.selectedFile = null;
      },
      error: (err) => {
        console.error('Upload failed', err);
        Swal.fire('Error', 'Failed to upload profile picture.', 'error');
      }
    });
  }

  updateInfo(): void {
    const { firstName, lastName, phoneNumber, address } = this.userProfile;
    if (!firstName || !lastName || !phoneNumber || !address) {
      Swal.fire('Error', 'Please fill in all fields.', 'error');
      return;
    }

    this.profileService.updateProfile(this.userProfile.profile_id, this.userProfile).subscribe({
      next: () => {
        Swal.fire('Success', 'Profile updated successfully!', 'success');
        this.originalProfile = JSON.parse(JSON.stringify(this.userProfile));
      },
      error: (err) => {
        console.error('Update failed', err);
        Swal.fire('Error', 'Failed to update profile.', 'error');
      }
    });
  }
  changePassword(): void {
    if (!this.passwordData.currentPassword || !this.passwordData.newPassword || !this.passwordData.confirmPassword) {
      Swal.fire('Error', 'Please fill in all password fields.', 'error');
      return;
    }

    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      Swal.fire('Error', 'New passwords do not match.', 'error');
      return;
    }

    this.auth.changePassword(
      this.passwordData.confirmPassword, this.passwordData.currentPassword
    ).subscribe({
      next: () => {
        Swal.fire('Success', 'Password changed successfully!', 'success');
        this.passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };
      },
      error: (err: { error: { message: any; }; }) => {
        console.error('Password change failed', err);
        Swal.fire('Error', err.error?.message || 'Failed to change password.', 'error');
      }
    });
  }

  isProfileChanged(): boolean {
    return JSON.stringify(this.userProfile) !== JSON.stringify(this.originalProfile);
  }

  isProfileValid(): boolean {
    const { firstName, lastName, phoneNumber, address } = this.userProfile;
    return !!(firstName?.trim() && lastName?.trim() && phoneNumber?.trim() && address?.trim());
  }

  deleteAccount(): void {
    Swal.fire({
      title: 'Are you sure?',
      text: "You won't be able to revert this! Your account will be permanently deleted.",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.profileService.deleteProfile(this.userProfile.profile_id).subscribe({
          next: () => {
            Swal.fire('Deleted!', 'Your account has been deleted.', 'success').then(() => {
              this.auth.logout();
              this.router.navigate(['/']);
            });
          },
          error: (err) => {
            console.error('Delete failed', err);
            Swal.fire('Error', 'Failed to delete account.', 'error');
          }
        });
      }
    });
  }
}
