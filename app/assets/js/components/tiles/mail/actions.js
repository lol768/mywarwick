export const MAIL_RECEIVE = 'mail.receive';

export function receiveMail(message) {
    return {
        type: MAIL_RECEIVE,
        message: message
    };
}