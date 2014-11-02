/*******************************************************************************
 *      Copyright (C) 2012 Google Inc.
 *      Licensed to The Android Open Source Project.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *******************************************************************************/

package com.android.mail.browse;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.SpannableString;

import com.android.mail.providers.ConversationInfo;
import com.android.mail.providers.ParticipantInfo;
import com.google.common.collect.Lists;

import java.util.ArrayList;

@SmallTest
public class SendersFormattingTests extends AndroidTestCase {

    private static ConversationInfo createConversationInfo() {
        return new ConversationInfo(0, 5, "snippet", "snippet", "snippet");
    }

    public void testMeFromNullName() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo(null, "something@somewhere.com", 0, false));
        final ArrayList<SpannableString> strings = Lists.newArrayList();
        assertEquals(0, strings.size());

        SendersView.format(getContext(), conv, "", 100, strings, null, null, null, false, false);
        assertEquals(1, strings.size());
        assertEquals("me", strings.get(0).toString());
    }

    public void testMeFromEmptyName() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo("", "something@somewhere.com", 0, false));
        final ArrayList<SpannableString> strings = Lists.newArrayList();
        assertEquals(0, strings.size());

        SendersView.format(getContext(), conv, "", 100, strings, null, null, null, false, false);
        assertEquals(1, strings.size());
        assertEquals("me", strings.get(0).toString());
    }

    public void testMeFromDuplicateEmptyNames() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo("", "something@somewhere.com", 0, false));
        conv.addParticipant(new ParticipantInfo("", "something@somewhere.com", 0, false));
        final ArrayList<SpannableString> strings = Lists.newArrayList();
        assertEquals(0, strings.size());

        SendersView.format(getContext(), conv, "", 100, strings, null, null, null, false, false);
        assertEquals(2, strings.size());
        assertNull(strings.get(0));
        assertEquals("me", strings.get(1).toString());
    }

    public void testDuplicates() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo("Something", "something@somewhere.com", 0, false));
        conv.addParticipant(new ParticipantInfo("Something", "something@somewhere.com", 0, false));

        final ArrayList<SpannableString> strings = Lists.newArrayList();
        assertEquals(0, strings.size());

        SendersView.format(getContext(), conv, "", 100, strings, null, null, null, false, false);
        assertEquals(2, strings.size());
        assertNull(strings.get(0));
        assertEquals("Something", strings.get(1).toString());
    }

    public void testSenderNameBadInput() {
        final ConversationInfo before = createConversationInfo();
        before.addParticipant(new ParticipantInfo("****^****", null, 0, false));

        final byte[] serialized = before.toBlob();

        final ConversationInfo after = ConversationInfo.fromBlob(serialized);
        assertEquals(1, after.participantInfos.size());
        assertEquals(before.participantInfos.get(0).name, after.participantInfos.get(0).name);
    }

    public void testConversationSnippetsBadInput() {
        final String first = "*^*";
        final String firstUnread = "*^*^*";
        final String last = "*^*^*^*";

        final ConversationInfo before = new ConversationInfo(42, 49, first, firstUnread, last);
        before.addParticipant(new ParticipantInfo("Foo Bar", "foo@bar.com", 0, false));
        assertEquals(first, before.firstSnippet);
        assertEquals(firstUnread, before.firstUnreadSnippet);
        assertEquals(last, before.lastSnippet);

        final byte[] serialized = before.toBlob();

        final ConversationInfo after = ConversationInfo.fromBlob(serialized);
        assertEquals(before.firstSnippet, after.firstSnippet);
        assertEquals(before.firstUnreadSnippet, after.firstUnreadSnippet);
        assertEquals(before.lastSnippet, after.lastSnippet);
    }

    public void testSenderAvatarIsSenderOfFirstUnreadMessage() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo("a", "a@a.com", 0, true));
        conv.addParticipant(new ParticipantInfo("b", "b@b.com", 0, false));
        conv.addParticipant(new ParticipantInfo("c", "c@c.com", 0, false));

        final ArrayList<SpannableString> styledSenders = Lists.newArrayList();
        final ArrayList<String> displayableSenderNames = Lists.newArrayList();
        final ConversationItemViewModel.SenderAvatarModel senderAvatarModel =
                new ConversationItemViewModel.SenderAvatarModel();

        SendersView.format(getContext(), conv, "", 100, styledSenders, displayableSenderNames,
                senderAvatarModel, null, false, false);

        assertEquals("b@b.com", senderAvatarModel.getEmailAddress());
        assertEquals("b", senderAvatarModel.getName());
    }

    public void testSenderAvatarIsLastSenderIfAllMessagesAreRead() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo("a", "a@a.com", 0, true));
        conv.addParticipant(new ParticipantInfo("b", "b@b.com", 0, true));
        conv.addParticipant(new ParticipantInfo("c", "c@c.com", 0, true));

        final ArrayList<SpannableString> styledSenders = Lists.newArrayList();
        final ArrayList<String> displayableSenderNames = Lists.newArrayList();
        final ConversationItemViewModel.SenderAvatarModel senderAvatarModel =
                new ConversationItemViewModel.SenderAvatarModel();

        SendersView.format(getContext(), conv, "", 100, styledSenders, displayableSenderNames,
                senderAvatarModel, null, false, false);

        assertEquals("c@c.com", senderAvatarModel.getEmailAddress());
        assertEquals("c", senderAvatarModel.getName());
    }

    public void testSenderAvatarIsLastSenderThatIsNotTheCurrentAccountIfAllMessagesAreRead() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo("a", "a@a.com", 0, true));
        conv.addParticipant(new ParticipantInfo("b", "b@b.com", 0, true));
        // empty name indicates it is the current account
        conv.addParticipant(new ParticipantInfo("", "c@c.com", 0, true));

        final ArrayList<SpannableString> styledSenders = Lists.newArrayList();
        final ArrayList<String> displayableSenderNames = Lists.newArrayList();
        final ConversationItemViewModel.SenderAvatarModel senderAvatarModel =
                new ConversationItemViewModel.SenderAvatarModel();

        SendersView.format(getContext(), conv, "", 100, styledSenders, displayableSenderNames,
                senderAvatarModel, null, false, false);

        assertEquals("b@b.com", senderAvatarModel.getEmailAddress());
        assertEquals("b", senderAvatarModel.getName());
    }

    public void testSenderAvatarIsCurrentAccountIfAllSendersAreCurrentAccount() {
        final ConversationInfo conv = createConversationInfo();
        // empty name indicates it is the current account
        conv.addParticipant(new ParticipantInfo("", "a@a.com", 0, true));

        final ArrayList<SpannableString> styledSenders = Lists.newArrayList();
        final ArrayList<String> displayableSenderNames = Lists.newArrayList();
        final ConversationItemViewModel.SenderAvatarModel senderAvatarModel =
                new ConversationItemViewModel.SenderAvatarModel();

        SendersView.format(getContext(), conv, "", 100, styledSenders, displayableSenderNames,
                senderAvatarModel, null, false, false);

        assertEquals("a@a.com", senderAvatarModel.getEmailAddress());
        assertEquals("", senderAvatarModel.getName());
    }

    /**
     * Two senders in a thread should be kept distinct if they have unique email addresses, even if
     * they happen to share the same name.
     */
    public void testSenderNamesWhenNamesMatchButEmailAddressesDiffer() {
        final ConversationInfo conv = createConversationInfo();
        conv.addParticipant(new ParticipantInfo("Andrew", "aholmes@awesome.com", 0, true));
        conv.addParticipant(new ParticipantInfo("Andrew", "ajohnson@wicked.com", 0, true));

        final ArrayList<SpannableString> styledSenders = Lists.newArrayList();
        final ArrayList<String> displayableSenderNames = Lists.newArrayList();
        final ConversationItemViewModel.SenderAvatarModel senderAvatarModel =
                new ConversationItemViewModel.SenderAvatarModel();

        SendersView.format(getContext(), conv, "", 100, styledSenders, displayableSenderNames,
                senderAvatarModel, null, false, false);

        assertEquals(2, displayableSenderNames.size());
        assertEquals("Andrew", displayableSenderNames.get(0));
        assertEquals("Andrew", displayableSenderNames.get(1));
    }
}