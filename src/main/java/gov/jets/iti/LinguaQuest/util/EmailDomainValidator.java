package gov.jets.iti.LinguaQuest.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

@Slf4j
@Component
public class EmailDomainValidator {

    public boolean hasValidMxRecord(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex).trim();
        String domain = email.substring(atIndex + 1).trim();

        if (localPart.isEmpty() || domain.isEmpty() || !domain.contains(".")) {
            return false;
        }

        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "1500");
        env.put("com.sun.jndi.dns.timeout.retries", "1");

        try {
            DirContext ictx = new InitialDirContext(env);
            Attributes attrs = ictx.getAttributes(domain, new String[]{"MX"});

            if (attrs != null && attrs.get("MX") != null && attrs.get("MX").size() > 0) {
                return true;
            }

            // Fallback check for A record (some domains use A records for mail)
            Attributes aAttrs = ictx.getAttributes(domain, new String[]{"A"});
            return aAttrs != null && aAttrs.get("A") != null && aAttrs.get("A").size() > 0;
        } catch (NamingException e) {
            log.warn("DNS lookup failed for domain [{}]: {}", domain, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during domain validation for [{}]: {}", domain, e.getMessage());
            // Fail open on system-level exception to avoid blocking registration if DNS context fails
            return true;
        }
    }
}
