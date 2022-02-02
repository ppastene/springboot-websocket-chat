<template>
    <div class="card mx-4 my-4">
        <div class="card-header">
            <ul class="nav nav-pills nav-header-pills">
                <li class="nav-item mr-2">
                    <button class="btn btn-success" type="button" v-on:click="connect()" v-if="!connected" :disabled="!message.username">Connect</button>
                </li>
                <li class="nav-item mr-2">
                    <input type="text" class="form-control" placeholder="Type your username" name="username" v-model="message.username" v-if="!connected">
                </li>
                <li class="nav-item">
                    <button class="btn btn-warning" type="button" v-on:click="disconnect()" v-if="connected">Disconnect</button>
                </li>
            </ul>
        </div>
        <div class="card-body scroll" id="card-body" v-if="connected">
            <ul class="list-group">
                <li class="list-group-item list-group-item-light" v-for="(message, index) in messages" :key="index.text">
                    <span v-if="message.type == 'NEW_USER'">
                        <!--{{ message.date | date:'shortTime' }} {{ message.text }} {{ message.username }}-->
                        {{ message.date }} {{ message.text }} <span v-bind:style="{ color: message.color }">{{ message.username }}</span>
                    </span>
                    <span v-else-if="message.type == 'MESSAGE'">
                        {{ message.date }} - <span v-bind:style="{ color: message.color }">{{ message.username }}</span> dice <br> {{ message.text }}
                    </span>
                </li>
            </ul>
        </div>
        <div class="card-footer" v-if="connected">
            <form class="form-inline" @submit.prevent="sendMessage()">
                <div class="form-group mx-3">
                    <input type="text" class="form-control" placeholder="Type something nice" name="text" v-model="message.text" v-on:keyup="writingEvent()">
                </div>
                <button class="btn btn-primary" type="submit" :disabled="!message.text">Send</button>
            </form>
            <div style="color: gray">&nbsp;&nbsp;&nbsp;{{ isWriting }}</div>
        </div>
    </div>
</template>
<script lang="ts">
import Vue from 'vue';
import { Client } from '@stomp/stompjs'
import * as SockJS from 'sockjs-client'
import { Message } from '../models/message'

export default Vue.extend({
    name: "Chat",
    data: ():any => ({
        client:Client,
        connected: false,
        message: new Message(),
        messages: new Array<Message>(),
        isWriting: String
    }),
    created():void {
        this.client = new Client();

        // Asignamos SockJS al STOMP
        this.client.webSocketFactory = () => {
            return new SockJS("http://localhost:8080/chat-websocket");
        }

        this.client.onConnect = (frame) => {
            console.log("Conectados: " + this.client.connected + ": "+frame);
            this.connected = true;

            this.client.subscribe('/chat/message', e => {
                let message:Message = JSON.parse(e.body) as Message;
                message.date = new Date(message.date);
                if(!this.message.color && this.message.type == 'NEW_USER' && this.message.username == message.username)
                {
                    this.message.color = message.color
                }
                this.messages.push(message)

                console.log(message.text)
            });

            this.client.subscribe('/chat/writing', e => {
                this.isWriting = e.body;
                setTimeout(() => this.isWriting = '', 3000);
            });

            this.message.type = "NEW_USER"
            this.client.publish({destination: '/app/message', body: JSON.stringify(this.message)})
        }
        
        this.client.onDisconnect = (frame) => {
            console.log("Desconectados: " + this.client.connected + ": "+frame);
            this.connected = false
        }
    },
    methods: {
        connect():void {
            this.client.activate();
        },
        disconnect():void {
            this.client.deactivate();
        },
        sendMessage():void {
            this.message.type = "MESSAGE"
            this.client.publish({destination: '/app/message', body: JSON.stringify(this.message)})
            this.message.text = null;
        },
        writingEvent():void{
            this.client.publish({destination: '/app/writing', body: JSON.stringify(this.message.username)})
        }
    }
})
</script>
<style>
    #card-body {
        overflow: scroll;
        height: 360px;
    }
</style>
