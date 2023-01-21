FROM node:14

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

# ARG 컨테이너가 시작될 때 실행되는 런타임 명령어 
ARG DEFAULT_PORT=80 

ENV PORT $DEFAULT_PORT

EXPOSE $PORT

# VOLUME [ "/app/node_modules" ]

CMD [ "npm", "start" ]