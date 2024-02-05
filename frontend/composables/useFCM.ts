// composable/useFCM.ts
import { useNuxtApp } from '#app';
import { getToken, type Messaging } from 'firebase/messaging';

interface UseFCM {
  token: Ref<string>;
  fetchToken: () => Promise<void>;
}

const useFCM = (): UseFCM => {
  const config = useRuntimeConfig();
  const token = ref<string>('');
  const { $fcm }: { $fcm: Messaging } = useNuxtApp();

  const fetchToken = async (): Promise<void> => {
    await getToken($fcm, {
      vapidKey: config.public.vapidKey,
    })
      .then((currentToken) => {
        token.value = currentToken;
      })
      .catch((err) => {
        console.error('An error occurred while retrieving token. ', err);
      });
  };

  return { token, fetchToken };
};

export default useFCM;