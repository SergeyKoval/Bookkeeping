import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/index';
import { User } from '../model/user';
import { SimpleResponse } from '../model/simple-response';

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  public constructor(private _http: HttpClient) { }

  public getAllUsers(): Observable<User[]> {
    return this._http.get<User[]>('/api/users/all');
  }

  public addUser(email: string, password: string, roles: string[]): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/users/add', {email: email, password: password, roles: roles});
  }

  public editUser(email: string, password: string, roles: string[]): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/users/edit', {email: email, password: password, roles: roles});
  }

  public deleteUser(email: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/users/delete', {email: email});
  }
}
