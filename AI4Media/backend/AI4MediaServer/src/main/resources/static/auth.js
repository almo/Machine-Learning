import { auth, provider } from "./firebase-config.js";
import { signInWithPopup, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js";

// Expose logout function to global scope so Alpine can use it
window.firebaseSignOut = () => signOut(auth);

// Expose token getter for API calls
window.getFirebaseAuthToken = async () => {
    // Resolves the race condition by explicitly waiting for Firebase's auth state to settle
    await auth.authStateReady();

    if (auth.currentUser) {
        return await auth.currentUser.getIdToken();
    }
    return null;
};

// Expose user ID getter for API payloads
window.getFirebaseUserId = () => auth.currentUser ? auth.currentUser.uid : "";

const btn = document.getElementById('google-signin-btn');
const status = document.getElementById('status');

if (btn) {
    btn.addEventListener('click', () => {
        signInWithPopup(auth, provider).catch((error) => {
            if(status) status.innerText = "Error: " + error.message;
        });
    });
}

onAuthStateChanged(auth, (user) => {
    if (user) {
        // Tell Alpine to switch to the main view (e.g. rss)
        window.dispatchEvent(new CustomEvent('auth-success'));
    } else {
        // Tell Alpine to return to login screen
        window.dispatchEvent(new CustomEvent('auth-logout'));
        if(status) status.innerText = "";
    }
});