package gov.jets.iti.LinguaQuest.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import gov.jets.iti.LinguaQuest.exception.auth.InvalidFirebaseTokenException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FirebaseTokenVerifierService {

    public FirebaseToken verifyIdToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.error("Firebase token verification failed! Reason: {}", e.getMessage(), e);
            throw new InvalidFirebaseTokenException("Firebase token is invalid or could not be verified");
        } catch (IllegalStateException e) {
            log.error("Firebase Admin SDK is uninitialized! Reason: {}", e.getMessage(), e);
            throw new InvalidFirebaseTokenException("Firebase service is uninitialized on server");
        } catch (IllegalArgumentException e) {
            log.error("Malformed Firebase token provided! Reason: {}", e.getMessage(), e);
            throw new InvalidFirebaseTokenException("Firebase token is invalid or malformed");
        }
    }
}
