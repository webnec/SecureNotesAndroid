# SecureNotes

### Android application made with:
- MVVM architecture, Android Jetpack, Google Architecture Components
- AndroidX Security to crypthographic
- AndroidX Biometric to authenthication
- BluetoothService

# Description
Application consists of four fragments.
<br>
The navigation between them is shown below.
![nav ](https://user-images.githubusercontent.com/50715560/132183961-f0094874-bc7f-49a3-bbcb-68795d6a195e.jpg)

## AuthFragment
- user authenticating and handle errors<br>

![authFragment](https://user-images.githubusercontent.com/50715560/132193046-f80b419e-34e1-4423-9202-5c109397a9dc.png)

## NotesFragment
- show notes
- save notes to file
- delete notes
- navigation to edit note fragment
- navigation to share via bluetooth fragment<br>

![notesfragment](https://user-images.githubusercontent.com/50715560/132190550-f57f8e85-6aeb-41e4-8b78-801cf31568a3.png)

## EditNoteFragment
- edit note
- add note
- delete note

![editFrag](https://user-images.githubusercontent.com/50715560/132190850-a887c92d-6750-415b-b216-8dc0efa6e863.png)

## ShareViaBluetoothFragment
- ask for enable bluetooth if disabled
- show list of paired devices
- connect to selected device 
- send / received data

![editFrag](https://user-images.githubusercontent.com/50715560/132192824-d0ba32d1-2256-423e-aead-fc31b954a925.png)

