package pe.edu.cibertec.CrudEstudiante.Configuracion;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		//obtenemos el token de la solicitud HTTP
		String token = obtenerJWTDeLaSolicitud(request);
		
		//validamos el token
		if(StringUtils.hasText(token) && jwtTokenProvider.validarToken(token)) {
			//obtenemos el username del token
			String username = jwtTokenProvider.obtenerUserNameDelJWT(token);
			//cargamos el usuario asociado al token
			UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
		    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		    //establecemos la seguridad
		    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		}
		filterChain.doFilter(request, response);
	}
	
	//Bearer token nos permite las autorizaciones
	private String obtenerJWTDeLaSolicitud(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer") ) {
			// recortamos del token el bearer para que solo mos quede el token
			return bearerToken.substring(7,bearerToken.length());
		}
		
		return null;
	}

}
