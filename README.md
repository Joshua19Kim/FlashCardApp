This project grade was 92/100(A+)<br/><br/>
**Development Process**<br/><br/>
My Flash Card app has been developed using Kotlin and Jetpack Compose, adhering to
modern Android development practices. The development process involved:
1. Setting up the project with Jetpack Compose
2. Creating data models and view models
3. Navigation between screens
4. Store data persistently
5. Show the simple animation
6. Validation check & using alarmDialog or T oast message<br/><br/>


**Features Implemented**<br/>(The feature of my app is slightly different from the requirement or the video that was
provided as a reference. But I believe that the implementation in my app is a little bit more
intuitive.)
1. App Icon will be shown with the animation(Fade-in & out) when user opens the app<br/><br/>
2. General feature :<br/>
● While rotating screen, the changes/data are retained on any screan<br/>
● App persists flash card data<br/><br/>
3. Home screen :<br/>
● ‘Create Flash card’ button<br/>
● ‘View Flash Cards(Int)’ button - On this button, it shows how many cards are
existing with bracket eg. View Flash Cards(4) means there are 4 cards in
database.<br/>
● ‘Play’ button - The big round button with icon was designed to be more
intuitive.<br/>
● Player’s name and ‘Change Name’ button - user can set up the player’s name
for the future record. This name would be saved persistently since this
information is not really significant with the current app feature.<br/>
● Back arrow button on nav bar & swipe gesture(left to right) - disabled as it is
not necessary.<br/>
● ‘Shuffle cards’ &’Shuffle options’ toggle button with Animation - If the toggle
button is ON, the animation will be shown and the card / option order will be
shuffled randomly for EACH Play. If OFF , the card / option order will be same
as the order in ‘View Flash Cards’<br/><br/>
4. Create / Edit Screen :<br/>
● Delete button(next to ‘Create a new flash card) / Back Arrow button on nav
bar / Swipe gesture(left to right)- show AlertDialog to ask to cancel & delete
current card.<br/>
● User cannot save a card with duplicated name that is already existing in
database<br/>
● User cannot have a card with duplicated options WITHIN the card<br/>
● A card requires at least one correct answer(tick box) and at least two options.
‘Delete’ button & check boxes will be abled only when the requirements are
met.<br/>
● User can set multiple correct answers ( I intentionally added this feature )<br/>
● All the validation error will be shown as toast message.<br/>
● On Edit screen, there is ‘Create card’ button, so user doesn’t need to go back
to ‘Home’ screen if necessary.<br/><br/>
5. Play / Result Screen :<br/>
● Starts with logo animation(Zoom in)<br/>
● User has to select at least one option to move to next question.<br/>
● User cannot go back to the previous question as the correctness will be
shown as T oast message as soon as user moves to the next question.<br/>
● Back arrow button on nav is to exit<br/>
● Swipe gesture(left to right) - Different T oast message will be shown depends
on what stage the user is on.<br/>
● The progress is in the top-right corner like ‘Progress 1 / 5’<br/><br/>
