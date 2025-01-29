import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UserAuthService } from '../user-auth/_service/user-auth.service';

export const AuthGuard: CanActivateFn = (route, state) => {

  const router = inject(Router);
  const userService = inject(UserAuthService);
  const token = userService.getToken();

  
  if(token !== null){
    return true;
  }else{
    router.navigate(['/auth/signin']);
    return false;
  }

};
