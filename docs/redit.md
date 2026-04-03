ukranian_dev

Привіт,
Дуже давно працюю над Gitember — десктопним Git-клієнтом . Свого часу не можна було віддавати на публіку (починався колись як експеримент ), потім не було бажання, потім часу ...  
Після порівняння з існуючімі аналогами ( топамі )  Gitember виглядає не тільки на рівні , але і має деякі переваги, наприклад пошук по історії та всередині  файлів ( офісні, креслення також підтримуються), вирішив оновити і щойно випустив версію 3.1.
Що є - покриває всі щоденні Git-операції - commit, branch, diff, rebase, LFS і т.д., але є кілька дійсно крутих речей, які сам постійно використовую :
пошук по історії навіть у не-текстових форматах (Office, DWG і т.п.)
порівняння довільних файлів і папок (не тільки в рамках Git)
Останнє  - в наш час супер коли треба швидко порівняти зміни після ШІ. Роблю акцент на простоті та швидкості, без перевантаження функціямии. Сайт тут https://gitember.org/ , код тут https://github.com/iazarny/gitember
Шукаю відгуки .


git
devtools
foss

programming - no

Hey, new version of Gitember is ready.
Main updates:
interactive rebase support (basic but usable)
ability to overwrite author & committer (useful when cleaning history)
experimental secret leak detection
some UI cleanup
It also covers everyday Git stuff (commit, branch, diff, etc.), but one thing I personally rely on a lot:
search through history including non-text formats (Office docs, DWG, PSD,  etc.)
arbitrary file/folder comparison
The last one very useful feature in our days, when need quikly compare a lot of AI changes
I’d really appreciate feedback . 
Site here https://gitember.org/ Code here https://github.com/iazarny/gitember 

