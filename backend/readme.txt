backend/
├── src/
│   ├── routes/
│   │   ├── auth.routes.js          # 로그인, 회원가입
│   │   ├── user.routes.js          # 마이페이지 관련
│   │   ├── recipe.routes.js        # 게시글 등록/조회
│   │   └── chat.routes.js          # 실시간 채팅
│
│   ├── controllers/
│   │   ├── auth.controller.js
│   │   ├── user.controller.js
│   │   ├── recipe.controller.js
│   │   └── chat.controller.js
│
│   ├── models/
│   │   ├── User.js
│   │   ├── Recipe.js
│   │   └── ChatMessage.js
│
│   ├── services/                  # DB 로직 or 비즈니스 로직
│   │   ├── user.service.js
│   │   └── recipe.service.js
│
│   ├── middlewares/              # 인증, 에러 처리 등
│   │   ├── auth.middleware.js
│   │   └── errorHandler.js
│
│   ├── utils/
│   │   └── sortHelper.js         # 좋아요순, 최신순 정렬
│
│   ├── config/
│   │   ├── db.js
│   │   └── socket.js             # Socket.io 설정
│
│   └── app.js                    # 앱 시작점
├── .env.example
├── package.json
└── README.md
