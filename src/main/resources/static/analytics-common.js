(function () {
  window.AnalyticsCommon = {
    getAuth() {
      const token = localStorage.getItem('civicpulse_token');
      const userStr = localStorage.getItem('civicpulse_user');
      if (!token || !userStr) {
        window.location.href = '/index.html';
        throw new Error('Not authenticated');
      }
      const user = JSON.parse(userStr);
      return { token, user };
    },

    authHeaders(token) {
      return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      };
    },

    async fetchJson(url, token) {
      const res = await fetch(url, { headers: this.authHeaders(token) });
      const payload = await res.json().catch(() => ({}));
      if (!res.ok) throw new Error(payload.error || 'Request failed');
      return payload;
    },

    palette() {
      return ['#0ea5e9', '#22c55e', '#f97316', '#ef4444', '#a855f7', '#f59e0b', '#14b8a6', '#64748b'];
    }
  };
})();