import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Podcast } from '../../entity';

@Injectable()
export class PodcastService {
	constructor(private http: HttpClient) {}

	findAll(): Observable<Podcast[]> {
		return this.http.get<Podcast[]>('/api/podcasts');
	}

  findById(id: string): Observable<Podcast> {
		return this.http.get<Podcast>(`/api/v1/podcasts/${id}`);
	}

	refresh(p: Podcast): Observable<void> {
		return this.http.get<void>(`/api/podcasts/${p.id}/update/force`);
	}
}
