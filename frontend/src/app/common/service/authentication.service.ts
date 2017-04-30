import {Inject, Injectable} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Http, Response} from '@angular/http';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';

import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs/Observable';
import {Md5} from 'ts-md5/dist/md5';
import {isNullOrUndefined} from 'util';

import {LoadingService} from 'app/common/service/loading.service';
import {HOST} from '../config/config';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/delay';

@Injectable()
export class AuthenticationService implements CanActivate {
  private _authenticationLoading: Subject<boolean>;
  private _authenticatedProfile: Profile;

  public constructor(
    private _router: Router,
    private _formBuilder: FormBuilder,
    private _loadingService: LoadingService,
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {
    this._authenticationLoading = _loadingService.authentication$$;
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!isNullOrUndefined(this._authenticatedProfile)) {
      return true;
    }

    this._router.navigate(['/authentication']);
    return false;
  }

  public initAuthenticationForm(): FormGroup {
    return this._formBuilder.group({
      email: ['', [Validators.required, Validators.email, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  public getProfileByEmail(email: string): Observable<Profile> {
    this._authenticationLoading.next(true);
    return this._http.get(`${this._host}/profiles?email=${email}`)
      .delay(1500)
      .map((response: Response) => {
        this._authenticationLoading.next(false);
        return response.json()[0];
      });
  }

  public authenticate(profile: Profile, password: string): boolean {
    if (Md5.hashStr(password) !== profile.password) {
      return false;
    }

    this._authenticatedProfile = profile;
    return true;
  }

  public exit(): void {
    this._authenticatedProfile = null;
  }

  public get authenticatedProfile(): Profile {
    return this._authenticatedProfile;
  }
}
