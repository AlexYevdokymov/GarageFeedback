Налаштування та запуск

1. Клонування репозиторію
git clone <URL_REPO>
cd <REPO_FOLDER>

2. Створення бота в Telegram
Зареєструйте бота у BotFather.
Отримайте BOT_TOKEN та BOT_USERNAME.
Створіть файл config.properties та помістіть його в корінь проекту.
Помістіть в файл назву бота та токен в такому вигляді:
bot.token=<Token>
bot.username=<Bot Name>

3. Налаштування бази PostgreSQL
Створіть базу даних для зберігання користувачів та відгуків.
Створіть файл hibernate.properties в src/main/resources з наповненням:

hibernate.connection.driver_class=org.postgresql.Driver
hibernate.connection.url=jdbc:postgresql://localhost:5432/<назва бд>
hibernate.connection.username=<логін>
hibernate.connection.password=<пароль>
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.show_sql=true
hibernate.hbm2ddl.auto=update

4. OpenAI API
Потрібно зареєструватись на платформі openai, створити API Key та отримати Token.
(Якщо реєструєтесь та створюєте API Key вперше - скоріш за все потрібно буде
додати картку для підтвердження акаунту та заплатити мінімальну суму 5$, які
будуть витрачатись на запити. Проте за 50 запитів не витратилось ще ні одного
центу. По розрахунках 5$ має вистачити на приблизно 200000 запитів даного боту)
Потрібно додати Token у файл config.properties у вигляді:
openai.token=<Token>

5. Google Docs API
Зареєструйтесь Google Cloud Console та отримайте Credentials у JSON форматі.
Назвіть вивантажений файл credentials.json та помістіть в src/main/resources.
Також потрібно створити документи та додати його id в файл config.properties
у вигляді:
googledocs.id=<google_docs_id>

7. Trello API
Створіть Trello Power-Up / API Key та Token.
Отримай API Key та Token та помістіть їх у файл config.properties у вигляді:
trello.apikey=<API Key>
trello.token=<Token>

Також потрібно створити картку у trello, в яку будуть додаватись відгуки,
та отримати її list id. Я отримав list_id відправивши у Postman запит GET
із посиланням на свою дошку і доданими API Key та Token
https://api.trello.com/1/boards/<id вашої дошки>/lists?key=<API Key>&token=<Token>
У відповіді буде JSON із списком листів, тобто карток, потрібно взяти id потрібної картки,
та додати в файл config.properties у вигляді:
trello.listid=<list id>


Для запуску бота - запустіть Java проект. Знайдіть в Telegram свого бота. Натисніть /start.
Після запуску потрібно обрати свою посаду, потім свою філію(регіон), і після цього можна
відправити у повідомленні відгук. Також можна натиснути кнопку "Змінити позицію або регіон"
та обрати нову позицію або новий регіон. Опрацювання надісланого відгуку можливе лише тоді, 
коли обрана і позиція, і регіон. На інших етапах відгук ніяк не опрацьовується. Відгуки
не прив'язані до id чату або юзера, тому є дійсно анонімними, якщо у цьому регіоні на цій
позиції працює не одна людина. Усі відгуки та можливе рішення від AI зберігаються в документі
Google Docs. Відгуки які мають критичність 4-5 потрапляють на дошку Trello. Інформація про
користувачів, а саме: chat id, обрана позиція, регіон, а також про відгуки, а саме: їх текст,
позиція та регіон того хто залишив, час та відповідь ChatGPT зберігаються в бд PostgreSQL.
