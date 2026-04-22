import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AuthService} from "./auth.service";

@Injectable({
    providedIn: 'root'
})
export class ProfileService {

    baseUrl = 'http://localhost:8070/api/profiles';
    constructor(private http: HttpClient, private authService: AuthService) {}

    getProfile() {
        const username = this.authService.getUserEmail();
        return this.http.get(`${this.baseUrl}/by-username/${username}`);
    }

    updateProfile(id: number, profile: any) {
        return this.http.put(`${this.baseUrl}/${id}`, profile);
    }

    uploadProfileImage(username: string, image: File) {
        const formData = new FormData();
        formData.append('image', image);
        return this.http.post(`${this.baseUrl}/add-profile-image/${username}`, formData);
    }

    deleteProfile(id: number) {
        return this.http.delete(`${this.baseUrl}/${id}`);
    }

}
