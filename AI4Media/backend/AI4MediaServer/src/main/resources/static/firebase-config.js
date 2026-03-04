import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";
import { getAuth, GoogleAuthProvider } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js";

const firebaseConfig = {
    apiKey: "AIzaSyA7Lu4dYSKDIcttm_TtzSLQNGHtRVKTAGM",
    authDomain: "meta-gear-464720-g3.firebaseapp.com",
    projectId: "meta-gear-464720-g3",
    storageBucket: "meta-gear-464720-g3.firebasestorage.app",
    messagingSenderId: "873718077819",
    appId: "1:873718077819:web:5380f57e84580cb7ea85de",
    measurementId: "G-VLK8TVSCLV"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const provider = new GoogleAuthProvider();