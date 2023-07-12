package jenkins;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.springframework.security.core.Authentication;

@Restricted(NoExternalUse.class)
public class ErrorAttributeFilter implements Filter {

    public static final String USER_ATTRIBUTE = "jenkins.ErrorAttributeFilter.user";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //final User currentUser = User.current();
        final Authentication authentication = Jenkins.getAuthentication2();
        servletRequest.setAttribute(USER_ATTRIBUTE, authentication);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}