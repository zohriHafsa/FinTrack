import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from '../services/keycloak.service';

// Endpoints the backend serves unauthenticated. A logged-in visitor's session
// token must never be attached here: Spring Security authenticates any Bearer
// token it's handed regardless of a permitAll rule, so a stale/expired token
// turns a "should just work" public request into a false 401.
const PUBLIC_PATHS = ['/auth/register', '/auth/login', '/auth/invite/', '/companies/sectors'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloakService = inject(KeycloakService);
  const token = keycloakService.getToken();
  const isPublic = PUBLIC_PATHS.some(path => req.url.includes(path));

  if (token && !isPublic) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  return next(req);
};